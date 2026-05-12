package com.example.serviceengineer

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import androidx.work.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ---------- Room сущности ----------
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val fullName: String,
    val email: String,
    val role: String
)

@Entity(tableName = "tokens")
data class TokenEntity(
    @PrimaryKey val id: Int = 1,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: Int,
    val machineId: Int,
    val address: String,
    val serviceType: String,
    val scheduledDate: String,
    val status: String,
    val rejectReason: String? = null,
    var isSynced: Boolean = false
)

@Entity(tableName = "protocols")
data class ProtocolEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val serviceType: String,
    val contentJson: String,
    val pdfPath: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

// ---------- Room DAO ----------
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
}

@Dao
interface TokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(token: TokenEntity)
    @Query("SELECT * FROM tokens WHERE id = 1")
    suspend fun getToken(): TokenEntity?
    @Delete
    suspend fun deleteToken(token: TokenEntity)
}

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
    @Query("SELECT * FROM orders ORDER BY scheduledDate DESC")
    suspend fun getAllOrders(): List<OrderEntity>
    @Update
    suspend fun updateOrder(order: OrderEntity)
    @Query("SELECT * FROM orders WHERE isSynced = 0")
    suspend fun getUnsyncedOrders(): List<OrderEntity>
}

@Dao
interface ProtocolDao {
    @Insert
    suspend fun insertProtocol(protocol: ProtocolEntity)
    @Query("SELECT * FROM protocols WHERE orderId = :orderId")
    suspend fun getProtocolForOrder(orderId: Int): ProtocolEntity?
}

@Dao
interface NoteDao {
    @Insert
    suspend fun insertNote(note: NoteEntity)
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    suspend fun getAllNotes(): List<NoteEntity>
}

@Database(entities = [UserEntity::class, TokenEntity::class, OrderEntity::class, ProtocolEntity::class, NoteEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tokenDao(): TokenDao
    abstract fun orderDao(): OrderDao
    abstract fun protocolDao(): ProtocolDao
    abstract fun noteDao(): NoteDao
}

// ---------- Retrofit API ----------
interface ApiService {
    @POST("auth/login")
    @FormUrlEncoded
    suspend fun login(@Field("email") email: String, @Field("password") password: String): Response<AuthResponse>

    @GET("orders")
    suspend fun getOrders(@Header("Authorization") token: String): Response<List<OrderDto>>

    @POST("orders/sync")
    suspend fun syncOrder(@Header("Authorization") token: String, @Body order: OrderDto): Response<Void>
}

data class AuthResponse(val accessToken: String, val refreshToken: String, val expiresIn: Long)
data class OrderDto(val id: Int, val machineId: Int, val address: String, val serviceType: String, val scheduledDate: String, val status: String)

// ---------- WorkManager для синхронизации ----------
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val prefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("access_token", null) ?: return Result.failure()
        val unsynced = db.orderDao().getUnsyncedOrders()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://your-server.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiService::class.java)
        for (order in unsynced) {
            try {
                val dto = OrderDto(order.id, order.machineId, order.address, order.serviceType, order.scheduledDate, order.status)
                val response = api.syncOrder("Bearer $token", dto)
                if (response.isSuccessful) {
                    db.orderDao().insertOrder(order.copy(isSynced = true))
                }
            } catch (e: Exception) {
                // retry later
            }
        }
        return Result.success()
    }
}

// ---------- MainActivity (один класс, содержащий всё) ----------
class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    // UI components
    private lateinit var authLayout: LinearLayout
    private lateinit var mainLayout: LinearLayout
    private lateinit var recyclerOrders: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var notesContainer: LinearLayout   // для списка заметок

    private var currentOrders = mutableListOf<OrderEntity>()
    private var speechRecognizer: SpeechRecognizer? = null

    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success -> /* handle */ }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> /* handle */ }

    companion object {
        private const val REQUEST_RECORD_AUDIO = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // нужно создать layout
        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        authLayout = findViewById(R.id.authLayout)
        mainLayout = findViewById(R.id.mainLayout)
        recyclerOrders = findViewById(R.id.recyclerOrders)
        bottomNav = findViewById(R.id.bottomNav)
        notesContainer = findViewById(R.id.notesContainer)

        ordersAdapter = OrdersAdapter { order -> showOrderDetails(order) }
        recyclerOrders.layoutManager = LinearLayoutManager(this)
        recyclerOrders.adapter = ordersAdapter

        setupAuth()
        setupBottomNav()

        checkTokenAndAutoLogin()
    }

    private fun setupAuth() {
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if (email.isNotBlank() && password.isNotBlank()) {
                performLogin(email, password)
            } else {
                Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogin(email: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // имитация API вызова (в реальности через Retrofit)
                val token = "fake-jwt-token-$email"
                val user = UserEntity(1, "Иванов Иван", email, "Инженер")
                db.userDao().insertUser(user)
                db.tokenDao().saveToken(TokenEntity(1, token, "refresh", System.currentTimeMillis() + 3600000))
                withContext(Dispatchers.Main) {
                    prefs.edit().putString("access_token", token).apply()
                    showMainScreen()
                    loadOrders()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка входа: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkTokenAndAutoLogin() {
        val token = prefs.getString("access_token", null)
        if (!token.isNullOrBlank()) {
            showMainScreen()
            loadOrders()
        } else {
            authLayout.visibility = View.VISIBLE
            mainLayout.visibility = View.GONE
        }
    }

    private fun showMainScreen() {
        authLayout.visibility = View.GONE
        mainLayout.visibility = View.VISIBLE
    }

    private fun loadOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            // имитация загрузки из API или БД
            val orders = listOf(
                OrderEntity(1, 101, "г. Москва, ул. Ленина 1", "Плановое", "2025-06-10", "Новая"),
                OrderEntity(2, 102, "г. СПб, Невский 50", "Аварийное", "2025-06-11", "Новая")
            )
            for (order in orders) {
                db.orderDao().insertOrder(order)
            }
            val dbOrders = db.orderDao().getAllOrders()
            withContext(Dispatchers.Main) {
                currentOrders.clear()
                currentOrders.addAll(dbOrders)
                ordersAdapter.submitList(currentOrders)
            }
        }
    }

    private fun showOrderDetails(order: OrderEntity) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_order_details, null)
        view.findViewById<TextView>(R.id.tvAddress).text = order.address
        view.findViewById<TextView>(R.id.tvServiceType).text = order.serviceType
        view.findViewById<TextView>(R.id.tvScheduledDate).text = order.scheduledDate
        val btnAccept = view.findViewById<Button>(R.id.btnAccept)
        val btnReject = view.findViewById<Button>(R.id.btnReject)
        val etRejectReason = view.findViewById<EditText>(R.id.etRejectReason)

        btnAccept.setOnClickListener {
            dialog.dismiss()
            if (order.serviceType == "Плановое" || order.serviceType == "Аварийное") {
                startProtocolActivity(order)
            } else {
                Toast.makeText(this, "Тип обслуживания не определён", Toast.LENGTH_SHORT).show()
            }
        }
        btnReject.setOnClickListener {
            val reason = etRejectReason.text.toString()
            if (reason.isBlank()) {
                Toast.makeText(this, "Укажите причину отказа", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val updated = order.copy(status = "Отклонена", rejectReason = reason)
                db.orderDao().updateOrder(updated)
                withContext(Dispatchers.Main) {
                    loadOrders()
                    Toast.makeText(this@MainActivity, "Наряд отклонён", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun startProtocolActivity(order: OrderEntity) {
        // для упрощения покажем диалог с чек-листом
        if (order.serviceType == "Плановое") {
            showPlannedProtocolDialog(order)
        } else {
            showEmergencyProtocolDialog(order)
        }
    }

    private fun showPlannedProtocolDialog(order: OrderEntity) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Протокол планового ТО")
            .setView(layoutInflater.inflate(R.layout.dialog_planned_protocol, null))
            .setPositiveButton("Сохранить") { d, _ ->
                val view = (d as AlertDialog).findViewById<View>(android.R.id.custom) ?: return@setPositiveButton
                val etCondition = view.findViewById<EditText>(R.id.etGeneralCondition).text.toString()
                val etWear = view.findViewById<EditText>(R.id.etWearLevel).text.toString()
                val etPressure = view.findViewById<EditText>(R.id.etPressure).text.toString()
                val etTemp = view.findViewById<EditText>(R.id.etTemperature).text.toString()
                val etCounter = view.findViewById<EditText>(R.id.etCounterValue).text.toString()
                val etPreventive = view.findViewById<EditText>(R.id.etPreventive).text.toString()
                // имитация фото (можно вызвать камеру)
                val photos = listOf("photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg")
                if (photos.size < 4) {
                    Toast.makeText(this, "Добавьте минимум 4 фото", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val content = """
                    Общее состояние: $etCondition
                    Износ: $etWear
                    Давление: $etPressure
                    Температура: $etTemp
                    Показания счетчика: $etCounter
                    Профилактика: $etPreventive
                    Фото: ${photos.joinToString()}
                """.trimIndent()
                saveProtocolAndGeneratePdf(order, content, photos)
            }
            .setNegativeButton("Отмена", null)
            .create()
        dialog.show()
    }

    private fun showEmergencyProtocolDialog(order: OrderEntity) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Протокол аварийного обслуживания")
            .setView(layoutInflater.inflate(R.layout.dialog_emergency_protocol, null))
            .setPositiveButton("Сохранить") { d, _ ->
                val view = (d as AlertDialog).findViewById<View>(android.R.id.custom) ?: return@setPositiveButton
                val description = view.findViewById<EditText>(R.id.etEmergencyDesc).text.toString()
                val causes = view.findViewById<EditText>(R.id.etCauses).text.toString()
                val measures = view.findViewById<EditText>(R.id.etMeasures).text.toString()
                val recommendations = view.findViewById<EditText>(R.id.etRecommendations).text.toString()
                val instructions = view.findViewById<EditText>(R.id.etInstructions).text.toString()
                val photos = listOf("emergency_photo.jpg")
                val content = """
                    Описание: $description
                    Причины: $causes
                    Меры: $measures
                    Рекомендации: $recommendations
                    Инструкции: $instructions
                    Фото: ${photos.joinToString()}
                """.trimIndent()
                saveProtocolAndGeneratePdf(order, content, photos)
            }
            .setNegativeButton("Отмена", null)
            .create()
        dialog.show()
    }

    private fun saveProtocolAndGeneratePdf(order: OrderEntity, content: String, photoUris: List<String>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pdfFile = generatePdf(order, content, photoUris)
            val protocol = ProtocolEntity(
                orderId = order.id,
                serviceType = order.serviceType,
                contentJson = content,
                pdfPath = pdfFile?.absolutePath
            )
            db.protocolDao().insertProtocol(protocol)
            // обновить статус заявки на "Закрыта"
            db.orderDao().updateOrder(order.copy(status = "Закрыта"))
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Протокол сохранён, PDF: ${pdfFile?.name}", Toast.LENGTH_LONG).show()
                loadOrders()
            }
        }
    }

    private fun generatePdf(order: OrderEntity, content: String, photoUris: List<String>): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        var y = 50f
        canvas.drawText("Протокол обслуживания", 50f, y, paint)
        y += 30f
        canvas.drawText("Наряд №${order.id}", 50f, y, paint)
        y += 30f
        canvas.drawText("Адрес: ${order.address}", 50f, y, paint)
        y += 30f
        for (line in content.lines()) {
            if (y > 800) break
            canvas.drawText(line.take(60), 50f, y, paint)
            y += 20f
        }
        pdfDocument.finishPage(page)
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "protocol_${order.id}_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_orders -> {
                    recyclerOrders.visibility = View.VISIBLE
                    notesContainer.visibility = View.GONE
                    loadOrders()
                    true
                }
                R.id.nav_notes -> {
                    recyclerOrders.visibility = View.GONE
                    notesContainer.visibility = View.VISIBLE
                    loadNotes()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadNotes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val notes = db.noteDao().getAllNotes()
            withContext(Dispatchers.Main) {
                notesContainer.removeAllViews()
                for (note in notes) {
                    val card = layoutInflater.inflate(R.layout.item_note, notesContainer, false)
                    card.findViewById<TextView>(R.id.tvNoteTitle).text = note.title
                    card.findViewById<TextView>(R.id.tvNoteContent).text = note.content
                    card.setOnClickListener { showNoteEditor(note) }
                    notesContainer.addView(card)
                }
                // кнопка добавления новой заметки
                val addBtn = Button(this@MainActivity).apply {
                    text = "+ Новая заметка"
                    setOnClickListener { showNoteEditor(null) }
                }
                notesContainer.addView(addBtn)
            }
        }
    }

    private fun showNoteEditor(note: NoteEntity?) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (note == null) "Новая заметка" else "Редактировать")
            .setView(layoutInflater.inflate(R.layout.dialog_note_editor, null))
            .setPositiveButton("Сохранить") { d, _ ->
                val view = (d as AlertDialog).findViewById<View>(android.R.id.custom) ?: return@setPositiveButton
                val title = view.findViewById<EditText>(R.id.etNoteTitle).text.toString()
                val content = view.findViewById<EditText>(R.id.etNoteContent).text.toString()
                if (title.isBlank()) {
                    Toast.makeText(this, "Введите заголовок", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    if (note == null) {
                        db.noteDao().insertNote(NoteEntity(title = title, content = content))
                    } else {
                        // обновление (проще удалить и вставить, или использовать update)
                        db.noteDao().insertNote(note.copy(title = title, content = content))
                    }
                    withContext(Dispatchers.Main) {
                        loadNotes()
                        Toast.makeText(this@MainActivity, "Заметка сохранена", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .create()
        if (note != null) {
            val view = dialog.findViewById<View>(android.R.id.custom)
            view?.findViewById<EditText>(R.id.etNoteTitle)?.setText(note.title)
            view?.findViewById<EditText>(R.id.etNoteContent)?.setText(note.content)
        }
        // голосовой ввод
        val voiceBtn = dialog.findViewById<Button>(R.id.btnVoiceInput)
        voiceBtn?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
                return@setOnClickListener
            }
            startVoiceInput(dialog)
        }
        dialog.show()
    }

    private fun startVoiceInput(dialog: AlertDialog) {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
        }
        speechRecognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = matches?.joinToString(" ") ?: ""
                val etContent = dialog.findViewById<EditText>(R.id.etNoteContent)
                etContent?.append(spoken)
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Toast.makeText(this@MainActivity, "Ошибка распознавания", Toast.LENGTH_SHORT).show()
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer?.startListening(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Разрешение получено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }

    // RecyclerView Adapter
    inner class OrdersAdapter(private val onClick: (OrderEntity) -> Unit) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {
        private var items = listOf<OrderEntity>()
        fun submitList(list: List<OrderEntity>) {
            items = list
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_card, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = items[position]
            holder.bind(order)
            holder.itemView.setOnClickListener { onClick(order) }
        }
        override fun getItemCount() = items.size
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(order: OrderEntity) {
                itemView.findViewById<TextView>(R.id.tvAddress).text = order.address
                itemView.findViewById<TextView>(R.id.tvServiceType).text = order.serviceType
                itemView.findViewById<TextView>(R.id.tvStatus).text = order.status
            }
        }
    }
}

// Singleton для Room
fun AppDatabase.getInstance(context: Context): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, "service_db")
        .fallbackToDestructiveMigration()
        .build()
}