Enter password: ****
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 18
Server version: 9.6.0 MySQL Community Server - GPL

Copyright (c) 2000, 2026, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> CREATE DATABASE vending_network;
Query OK, 1 row affected (0.312 sec)

mysql> USE vending_network;
Database changed
mysql> CREATE TABLE VendingMachines (
    -> MachineID INT PRIMARY KEY,
    -> Location VARCHAR(255) NOT NULL,
    -> Model VARCHAR(100) NOT NULL,
    -> PaymentType ENUM('с оплатой картой', 'с оплатой наличными', 'два вида оплаты') NOT NULL,
    -> FullIncome DECIMAL(12,2) DEFAULT 0,
    -> SerialNumber VARCHAR(50) UNIQUE NOT NULL,
    -> InventoryNumber VARCHAR(50) UNIQUE NOT NULL,
    -> Manufacturer VARCHAR(100),
    -> ManufactureDate DATE NOT NULL,
    -> DateOfCommissioning DATE NOT NULL,
    -> LastVerificationDate DATE NOT NULL,
    -> VerificationInterval INT NOT NULL,
    -> ResourceHours INT,
    -> DateOfNextFixing DATE NOT NULL,
    -> MaintenanceTimeHours INT,
    -> MachineStatus ENUM('Работает', 'Вышел из строя', 'В ремонте/на обслуживании') NOT NULL,
    -> Country VARCHAR(50),
    -> InventoryDate DATE NOT NULL,
    -> LastCheckedByUser VARCHAR(100),
    -> NextScheduledMaintenance DATE,
    -> ModemID INT DEFAULT -1,
    -> Company VARCHAR(100) DEFAULT 'Франчайзи'
    -> );
Query OK, 0 rows affected (0.309 sec)

mysql> CREATE TABLE VendingMachines (
    ->
    ->
    -> CREATE TABLE Products (
    -> ProductID INT PRIMARY KEY,
    -> Name VARCHAR(100) NOT NULL,
    -> Description TEXT,
    -> Price DECIMAL(10,2) NOT NULL,
    -> InStock INT NOT NULL,
    -> MinStock INT NOT NULL,
    -> PropensityToSell DECIMAL(5,2)
    -> );
ERROR 1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 'CREATE TABLE Products (
ProductID INT PRIMARY KEY,
Name VARCHAR(100) NOT NULL,
D' at line 4
mysql> CREATE TABLE Products (;
ERROR 1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '' at line 1
mysql> ^C
mysql> ^C
mysql> CREATE TABLE Products ( ProductID INT PRIMARY KEY, Name VARCHAR(100) NOT NULL, Description TEXT, Price DECIMAL(10,2) NOT NULL, InStock INT NOT NULL, MinStock INT NOT NULL, PropensityToSell DECIMAL(5,2) )
    ->
    ->
    -> ^A^C
mysql> ^C
mysql> ^C
mysql> ^C
mysql> CREATE TABLE Products ( ProductID INT PRIMARY KEY, Name VARCHAR(100) NOT NULL, Description TEXT, Price DECIMAL(10,2) NOT NULL, InStock INT NOT NULL, MinStock INT NOT NULL, PropensityToSell DECIMAL(5,2) );
Query OK, 0 rows affected (0.156 sec)

mysql> CREATE TABLE Sales ( SaleID INT PRIMARY KEY, MachineID INT, ProductID INT, Quantity INT NOT NULL, SaleSum DECIMAL(10,2) NOT NULL, SaleDateTime DATETIME NOT NULL, PaymentType VARCHAR(20), FOREIGN KEY (MachineID) REFERENCES VendingMachines(MachineID), FOREIGN KEY (ProductID) REFERENCES Products(ProductID) );
Query OK, 0 rows affected (0.727 sec)

mysql> CREATE TABLE Users ( UserID INT PRIMARY KEY, FullName VARCHAR(150) NOT NULL, Contacts VARCHAR(200), Role ENUM('Администратор', 'Оператор') NOT NULL, Login VARCHAR(100) UNIQUE, PasswordHash VARCHAR(255) );
Query OK, 0 rows affected (0.514 sec)

mysql> CREATE TABLE Employees ( EmployeeID INT PRIMARY KEY AUTO_INCREMENT, FullName VARCHAR(150) NOT NULL, Phone VARCHAR(20), Email VARCHAR(100), SupportedModels TEXT, MaxTasksPerWeek INT DEFAULT 15, MaxTasksPerDay INT DEFAULT 4 );
Query OK, 0 rows affected (0.120 sec)

mysql> CREATE TABLE ServiceRequests ( RequestID INT PRIMARY KEY AUTO_INCREMENT, MachineID INT, ScheduledDate DATE NOT NULL, Type ENUM('Плановое', 'Авария') NOT NULL, Priority INT DEFAULT 1, Status ENUM('Новая', 'В работе', 'Закрыта', 'Отменена') DEFAULT 'Новая', AssignedTo INT, RejectReason TEXT, CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (MachineID) REFERENCES VendingMachines(MachineID), FOREIGN KEY (AssignedTo) REFERENCES Employees(EmployeeID) );
Query OK, 0 rows affected (0.544 sec)

mysql> CREATE TABLE ServiceReports ( ReportID INT PRIMARY KEY AUTO_INCREMENT, RequestID INT, MachineID INT, InspectionDate DATE NOT NULL, EngineerName VARCHAR(150), ChecklistData JSON, PdfPath VARCHAR(255), CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (RequestID) REFERENCES ServiceRequests(RequestID), FOREIGN KEY (MachineID) REFERENCES VendingMachines(MachineID) );
Query OK, 0 rows affected (0.548 sec)

mysql> CREATE TABLE StatusHistory ( HistoryID INT PRIMARY KEY AUTO_INCREMENT, EntityType ENUM('Заявка', 'ТА') NOT NULL, EntityID INT NOT NULL, OldStatus VARCHAR(50), NewStatus VARCHAR(50), ChangedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, ChangedBy VARCHAR(100) );
Query OK, 0 rows affected (0.139 sec)

mysql> CREATE TABLE Notifications ( NotifID INT PRIMARY KEY AUTO_INCREMENT, MachineID INT, Message TEXT, Type ENUM('Критическая', 'Предупреждение', 'Информационная'), CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, IsRead BOOLEAN DEFAULT FALSE, FOREIGN KEY (MachineID) REFERENCES VendingMachines(MachineID) );
Query OK, 0 rows affected (0.308 sec)

mysql> SHOW TABLES;
+---------------------------+
| Tables_in_vending_network |
+---------------------------+
| employees                 |
| notifications             |
| products                  |
| sales                     |
| servicereports            |
| servicerequests           |
| statushistory             |
| users                     |
| vendingmachines           |
+---------------------------+
9 rows in set (0.045 sec)

mysql> CREATE TABLE Products ( ProductID INT PRIMARY KEY, Name VARCHAR(100) NOT NULL, Description TEXT, Price DECIMAL(10,2) NOT NULL, InStock INT NOT NULL, MinStock INT NOT NULL, PropensityToSell DECIMAL(5,2) );
ERROR 1050 (42S01): Table 'products' already exists
mysql> SELECT COUNT(*) FROM VendingMachines;
+----------+
| COUNT(*) |
+----------+
|        0 |
+----------+
1 row in set (0.030 sec)

mysql>
mysql> INSERT INTO VendingMachines (MachineID, Location, Model, PaymentType, FullIncome, SerialNumber, InventoryNumber, Manufacturer, ManufactureDate, DateOfCommissioning, LastVerificationDate, VerificationInterval, ResourceHours, DateOfNextFixing, MaintenanceTimeHours, MachineStatus, Country, InventoryDate, LastCheckedByUser) VALUES
    -> (1, 'г. Санкт‑Петербург, Невский пр., д. 50, ТЦ «Галерея», 2‑й этаж.', 'VendCore X-200', 'с оплатой картой', 1250000, 'SC123456789', 'INV-2025-001', 'ООО «ВендТех»', '2025-05-01', '2025-05-10', '2025-06-15', 6, 2500, '2026-08-01', 4, 'Работает', 'Россия', '2025-07-20', 'Иванов А. С.'),
    -> (2, 'Московская обл., г. Химки, ул. Московская, д. 15, офис 301.', 'CoffeeMaster Pro 500', 'с оплатой наличными', 1250000, 'SN987654321', 'INV-2025-002', 'АО «КофеМаш»', '2025-06-15', '2025-06-20', '2025-07-25', 12, 1800, '2026-09-10', 8, 'Вышел из строя', 'Китай', '2025-08-15', 'Петрова М. И.'),
    -> (3, 'г. Казань, ул. Баумана, д. 20, кафетерий', 'SnackVend S-300', 'два вида оплаты', 1250000, 'VCX200-001', 'INV-2025-003', 'ЗАО «СнекВенд»', '2025-07-20', '2025-07-22', '2025-08-05', 24, 1801, '2026-10-20', 12, 'В ремонте/на обслуживании', 'Германия', '2025-09-10', 'Сидоров Д. В.'),
    -> (4, 'г. Екатеринбург, ул. Ленина, д. 50, холл бизнес‑центра.', 'AquaVend Water 2025', 'с оплатой картой', 1250000, 'CM500-PRO-002', 'INV-2025-004', 'ООО «АкваВенд»', '2025-08-10', '2025-08-15', '2025-09-20', 18, 1802, '2026-11-05', 6, 'Работает', 'Южная Корея', '2025-09-10', 'Кузнецова Е. П.'),
    -> (5, 'г. Новосибирск, Красный пр., д. 100, университетский кампус.', 'VendoTech Elite 400', 'два вида оплаты', 1250000, 'SV300-SN003', 'INV-2025-005', 'ООО «ТехноВенд»', '2025-09-25', '2025-09-30', '2025-10-01', 36, 1803, '2026-12-15', 16, 'В ремонте/на обслуживании', 'США', '2025-11-10', 'Морозов Р. Н.'),
    -> (6, 'г. Сочи, Курортный пр., д. 70, отель «Морская звезда», лобби.', 'QuickBite Mini 100', 'с оплатой наличными', 1250000, 'QB100-MIN-006', 'INV-2025-006', 'ИП «МиниВенд»', '2025-10-05', '2025-10-10', '2025-11-15', 12, 1804, '2026-03-25', 10, 'Вышел из строя', 'Италия', '2025-12-10', 'Волкова Т. Л.'),
    -> (7, 'г. Нижний Новгород, ул. Большая Покровская, д. 40, торговый пассаж.', 'HotDrink Station 600', 'с оплатой картой', 1250000, 'VT400-ELT-005', 'INV-2025-007', 'ООО «ГорячийНапиток»', '2025-11-12', '2025-11-15', '2025-12-22', 6, 1805, '2026-03-03', 3, 'Работает', 'Турция', '2025-12-10', 'Алексеев С. М.'),
    -> (8, 'г. Самара, ул. Молодогвардейская, д. 120, ТЦ «Мега».', 'FreshFood Vend 700', 'два вида оплаты', 1250000, 'FF700-VND-008', 'INV-2025-008', 'АО «ФрэшФудВенд»', '2025-12-18', '2025-12-20', '2026-01-05', 24, 1806, '2026-04-12', 18, 'В ремонте/на обслуживании', 'Япония', '2026-01-10', 'Никитина О. А.'),
    -> (9, 'г. Ростов‑на‑Дону, ул. Садовая, д. 80, административное здание.', 'IceCream Vend 250', 'с оплатой наличными', 1250000, 'HDS600-007', 'INV-2025-009', 'ООО «АйсВенд»', '2026-01-03', '2026-01-10', '2026-01-12', 18, 1807, '2026-05-22', 7, 'Вышел из строя', 'Польша', '2026-01-13', 'Фёдоров И. Б.'),
    -> (10, 'г. Владивосток, ул. Светланская, д. 60, морской вокзал.', 'Print&Go Kiosk 150', 'с оплатой картой', 1250000, 'VT400-ELT-005', 'INV-2025-010', 'ЗАО «ПринтВенд»', '2026-01-14', '2026-01-20', '2026-01-25', 12, 1808, '2026-07-02', 14, 'Работает', 'Тайвань', '2026-01-22', 'Григорьева Н. К.');
ERROR 1062 (23000): Duplicate entry 'VT400-ELT-005' for key 'vendingmachines.SerialNumber'
mysql> TRUNCATE TABLE VendingMachines;
ERROR 1701 (42000): Cannot truncate a table referenced in a foreign key constraint (`vending_network`.`sales`, CONSTRAINT `sales_ibfk_1`)
mysql>
mysql>
mysql> INSERT INTO Products (ProductID, Name, Description, Price, InStock, MinStock, PropensityToSell) VALUES
    -> (1, 'Кофе «Эспрессо»', 'Эспрессо из 100% арабики, без добавок. Объём: 250 мл', 120, 18, 5, 3.5),
    -> (2, 'Чипсы «Сыр и Лук»', 'Картофельные чипсы с ароматом сыра и лука. Без ГМО', 95, 25, 8, 2.1),
    -> (3, 'Вода минеральная негазированная', 'Природная минеральная вода, низкоминерализованная. Без газа', 60, 40, 10, 4.8),
    -> (4, 'Шоколадный батончик «Ореховый восторг»', 'Молочный шоколад с цельным фундуком и карамельной начинкой', 85, 30, 7, 1.9),
    -> (5, 'Газированный напиток «Кола»', 'Газированный напиток со вкусом колы, с кофеином', 75, 22, 6, 2.7),
    -> (6, 'Смесь орехов «Классика»', 'Смесь миндаля, фундука и грецкого ореха, слегка подсоленная', 150, 15, 4, 1.2),
    -> (7, 'Леденцы «Мятные»', 'Мятные леденцы без сахара, с натуральным ароматизатором', 45, 50, 12, 5.3),
    -> (8, 'Попкорн «Сливочный»', 'Воздушный попкорн со сливочным маслом и солью', 70, 28, 9, 1.8),
    -> (9, 'Энергетический напиток «Turbo»', 'Энергетический напиток с таурином, кофеином и витаминами группы B', 130, 12, 3, 2.4);
Query OK, 9 rows affected (0.302 sec)
Records: 9  Duplicates: 0  Warnings: 0

mysql>
mysql> INSERT INTO Sales (SaleID, MachineID, ProductID, Quantity, SaleSum, SaleDateTime, PaymentType) VALUES
    -> (1, 2, 9, 1, 120, '2026-01-22 08:15:30', 'Карта'),
    -> (2, 5, 2, 3, 285, '2026-01-22 10:45:12', 'Наличные'),
    -> (3, 9, 6, 2, 300, '2026-01-22 12:30:45', 'QR-код'),
    -> (4, 8, 5, 1, 85, '2026-01-22 14:20:05', 'Карта'),
    -> (5, 6, 7, 4, 180, '2026-01-22 16:55:22', 'Наличные'),
    -> (6, 1, 1, 1, 120, '2026-01-22 18:03:17', 'QR-код'),
    -> (7, 3, 4, 5, 425, '2026-01-22 19:40:50', 'Карта'),
    -> (8, 10, 2, 2, 190, '2026-01-22 21:10:33', 'Наличные'),
    -> (9, 4, 8, 1, 70, '2026-01-22 22:50:47', 'QR-код'),
    -> (10, 7, 7, 3, 135, '2026-01-22 23:59:01', 'Карта');
ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails (`vending_network`.`sales`, CONSTRAINT `sales_ibfk_1` FOREIGN KEY (`MachineID`) REFERENCES `vendingmachines` (`MachineID`))
mysql>
mysql> INSERT INTO Sales (SaleID, MachineID, ProductID, Quantity, SaleSum,;
ERROR 1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '' at line 1
mysql> ^C
mysql>
mysql> INSERT INTO Sales (SaleID, MachineID, ProductID, Quantity, SaleSum, SaleDateTime, PaymentType) VALUES
    -> (1, 2, 9, 1, 120, '2026-01-22 08:15:30', 'Карта'),
    -> (2, 5, 2, 3, 285, '2026-01-22 10:45:12', 'Наличные'),
    -> (3, 9, 6, 2, 300, '2026-01-22 12:30:45', 'QR-код'),
    -> (4, 8, 5, 1, 85, '2026-01-22 14:20:05', 'Карта'),
    -> (5, 6, 7, 4, 180, '2026-01-22 16:55:22', 'Наличные'),
    -> (6, 1, 1, 1, 120, '2026-01-22 18:03:17', 'QR-код'),
    -> (7, 3, 4, 5, 425, '2026-01-22 19:40:50', 'Карта'),
    -> (8, 10, 2, 2, 190, '2026-01-22 21:10:33', 'Наличные'),
    -> (9, 4, 8, 1, 70, '2026-01-22 22:50:47', 'QR-код'),
    -> (10, 7, 7, 3, 135, '2026-01-22 23:59:01', 'Карта');
ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails (`vending_network`.`sales`, CONSTRAINT `sales_ibfk_1` FOREIGN KEY (`MachineID`) REFERENCES `vendingmachines` (`MachineID`))
mysql>
mysql> ^C
mysql> ^C
mysql>
mysql> INSERT INTO Sales (SaleID, MachineID, ProductID, Quantity, SaleSum, SaleDateTime, PaymentType) VALUES
    -> (1, 2, 9, 1, 120, '2026-01-22 08:15:30', 'Карта'),
    -> (2, 5, 2, 3, 285, '2026-01-22 10:45:12', 'Наличные'),
    -> (3, 9, 6, 2, 300, '2026-01-22 12:30:45', 'QR-код'),
    -> (4, 8, 5, 1, 85, '2026-01-22 14:20:05', 'Карта'),
    -> (5, 6, 7, 4, 180, '2026-01-22 16:55:22', 'Наличные'),
    -> (6, 1, 1, 1, 120, '2026-01-22 18:03:17', 'QR-код'),
    -> (7, 3, 4, 5, 425, '2026-01-22 19:40:50', 'Карта'),
    -> (8, 10, 2, 2, 190, '2026-01-22 21:10:33', 'Наличные'),
    -> (9, 4, 8, 1, 70, '2026-01-22 22:50:47', 'QR-код'),
    -> (10, 7, 7, 3, 135, '2026-01-22 23:59:01', 'Карта');
ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails (`vending_network`.`sales`, CONSTRAINT `sales_ibfk_1` FOREIGN KEY (`MachineID`) REFERENCES `vendingmachines` (`MachineID`))
mysql> SET FOREIGN_KEY_CHECKS = 1;
Query OK, 0 rows affected (0.004 sec)

mysql> SELECT COUNT(*) FROM Sales;
+----------+
| COUNT(*) |
+----------+
|        0 |
+----------+
1 row in set (0.008 sec)

mysql>
mysql> SET FOREIGN_KEY_CHECKS = 0;
Query OK, 0 rows affected (0.003 sec)

mysql> DELETE FROM VendingMachines;
Query OK, 0 rows affected (0.004 sec)

mysql> INSERT INTO VendingMachines (MachineID, Location, Model, PaymentType, FullIncome, SerialNumber, InventoryNumber, Manufacturer, ManufactureDate, DateOfCommissioning, LastVerificationDate, VerificationInterval, ResourceHours, DateOfNextFixing, MaintenanceTimeHours, MachineStatus, Country, InventoryDate, LastCheckedByUser) VALUES
    -> (1, 'г. Санкт‑Петербург, Невский пр., д. 50, ТЦ «Галерея», 2‑й этаж.', 'VendCore X-200', 'с оплатой картой', 1250000, 'SC123456789', 'INV-2025-001', 'ООО «ВендТех»', '2025-05-01', '2025-05-10', '2025-06-15', 6, 2500, '2026-08-01', 4, 'Работает', 'Россия', '2025-07-20', 'Иванов А. С.'),
    -> (2, 'Московская обл., г. Химки, ул. Московская, д. 15, офис 301.', 'CoffeeMaster Pro 500', 'с оплатой наличными', 1250000, 'SN987654321', 'INV-2025-002', 'АО «КофеМаш»', '2025-06-15', '2025-06-20', '2025-07-25', 12, 1800, '2026-09-10', 8, 'Вышел из строя', 'Китай', '2025-08-15', 'Петрова М. И.'),
    -> (3, 'г. Казань, ул. Баумана, д. 20, кафетерий', 'SnackVend S-300', 'два вида оплаты', 1250000, 'VCX200-001', 'INV-2025-003', 'ЗАО «СнекВенд»', '2025-07-20', '2025-07-22', '2025-08-05', 24, 1801, '2026-10-20', 12, 'В ремонте/на обслуживании', 'Германия', '2025-09-10', 'Сидоров Д. В.'),
    -> (4, 'г. Екатеринбург, ул. Ленина, д. 50, холл бизнес‑центра.', 'AquaVend Water 2025', 'с оплатой картой', 1250000, 'CM500-PRO-002', 'INV-2025-004', 'ООО «АкваВенд»', '2025-08-10', '2025-08-15', '2025-09-20', 18, 1802, '2026-11-05', 6, 'Работает', 'Южная Корея', '2025-09-10', 'Кузнецова Е. П.'),
    -> (5, 'г. Новосибирск, Красный пр., д. 100, университетский кампус.', 'VendoTech Elite 400', 'два вида оплаты', 1250000, 'SV300-SN003', 'INV-2025-005', 'ООО «ТехноВенд»', '2025-09-25', '2025-09-30', '2025-10-01', 36, 1803, '2026-12-15', 16, 'В ремонте/на обслуживании', 'США', '2025-11-10', 'Морозов Р. Н.'),
    -> (6, 'г. Сочи, Курортный пр., д. 70, отель «Морская звезда», лобби.', 'QuickBite Mini 100', 'с оплатой наличными', 1250000, 'QB100-MIN-006', 'INV-2025-006', 'ИП «МиниВенд»', '2025-10-05', '2025-10-10', '2025-11-15', 12, 1804, '2026-03-25', 10, 'Вышел из строя', 'Италия', '2025-12-10', 'Волкова Т. Л.'),
    -> (7, 'г. Нижний Новгород, ул. Большая Покровская, д. 40, торговый пассаж.', 'HotDrink Station 600', 'с оплатой картой', 1250000, 'VT400-ELT-005', 'INV-2025-007', 'ООО «ГорячийНапиток»', '2025-11-12', '2025-11-15', '2025-12-22', 6, 1805, '2026-03-03', 3, 'Работает', 'Турция', '2025-12-10', 'Алексеев С. М.'),
    -> (8, 'г. Самара, ул. Молодогвардейская, д. 120, ТЦ «Мега».', 'FreshFood Vend 700', 'два вида оплаты', 1250000, 'FF700-VND-008', 'INV-2025-008', 'АО «ФрэшФудВенд»', '2025-12-18', '2025-12-20', '2026-01-05', 24, 1806, '2026-04-12', 18, 'В ремонте/на обслуживании', 'Япония', '2026-01-10', 'Никитина О. А.'),
    -> (9, 'г. Ростов‑на‑Дону, ул. Садовая, д. 80, административное здание.', 'IceCream Vend 250', 'с оплатой наличными', 1250000, 'HDS600-007', 'INV-2025-009', 'ООО «АйсВенд»', '2026-01-03', '2026-01-10', '2026-01-12', 18, 1807, '2026-05-22', 7, 'Вышел из строя', 'Польша', '2026-01-13', 'Фёдоров И. Б.'),
    -> (10, 'г. Владивосток, ул. Светланская, д. 60, морской вокзал.', 'Print&Go Kiosk 150', 'с оплатой картой', 1250000, 'PK150-010', 'INV-2025-010', 'ЗАО «ПринтВенд»', '2026-01-14', '2026-01-20', '2026-01-25', 12, 1808, '2026-07-02', 14, 'Работает', 'Тайвань', '2026-01-22', 'Григорьева Н. К.');
Query OK, 10 rows affected (0.276 sec)
Records: 10  Duplicates: 0  Warnings: 0

mysql> INSERT INTO Sales (SaleID, MachineID, ProductID, Quantity, SaleSum, SaleDateTime, PaymentType) VALUES
    -> (1, 2, 9, 1, 120, '2026-01-22 08:15:30', 'Карта'),
    -> (2, 5, 2, 3, 285, '2026-01-22 10:45:12', 'Наличные'),
    -> (3, 9, 6, 2, 300, '2026-01-22 12:30:45', 'QR-код'),
    -> (4, 8, 5, 1, 85, '2026-01-22 14:20:05', 'Карта'),
    -> (5, 6, 7, 4, 180, '2026-01-22 16:55:22', 'Наличные'),
    -> (6, 1, 1, 1, 120, '2026-01-22 18:03:17', 'QR-код'),
    -> (7, 3, 4, 5, 425, '2026-01-22 19:40:50', 'Карта'),
    -> (8, 10, 2, 2, 190, '2026-01-22 21:10:33', 'Наличные'),
    -> (9, 4, 8, 1, 70, '2026-01-22 22:50:47', 'QR-код'),
    -> (10, 7, 7, 3, 135, '2026-01-22 23:59:01', 'Карта');
Query OK, 10 rows affected (0.247 sec)
Records: 10  Duplicates: 0  Warnings: 0

mysql> INSERT INTO Users (UserID, FullName, Contacts, Role, Login, PasswordHash) VALUES
    -> (1, 'Иванов Алексей Петрович', 'alex.ivanov@example.com, +7 916 123-45-67', 'Администратор', 'ivanov_a', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 2
    '> ,
    '> ′
    '> ПетроваМарияИвановн
    '> а
    '> ′
    '> ,
    '> ′
    '> m
    '> a
    '> r
    '> i
    '> a
    '> .
    '> p
    '> e
    '> t
    '> r
    '> o
    '> v
    '> a
    '> @
    '> m
    '> a
    '> i
    '> l
    '> .
    '> r
    '> u
    '> ,
    '> +
    '> 7903234
    '> −
    '> 56
    '> −
    '> 78
    '> ′
    '> ,
    '> ′
    '> Операто
    '> р
    '> ′
    '> ,
    '> ′
    '> p
    '> e
    '> t
    '> r
    '> o
    '> v
    '> a
    '> m
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(2,
    '> ′
    '>  ПетроваМарияИвановна
    '> ′
    '>  ,
    '> ′
    '>  maria.petrova@mail.ru,+7903234−56−78
    '> ′
    '>  ,
    '> ′
    '>  Оператор
    '> ′
    '>  ,
    '> ′
    '>  petrova
    '> m
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123'),
    -> (3, 'Сидоров Дмитрий Викторович', 'dmitry.sidorov@yandex.ru, +7 926 345-67-89', 'Оператор', 'sidorov_d', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 4
    '> ,
    '> ′
    '> КузнецоваЕленаПавловн
    '> а
    '> ′
    '> ,
    '> ′
    '> e
    '> l
    '> e
    '> n
    '> a
    '> .
    '> k
    '> u
    '> z
    '> n
    '> e
    '> t
    '> s
    '> o
    '> v
    '> a
    '> @
    '> g
    '> m
    '> a
    '> i
    '> l
    '> .
    '> c
    '> o
    '> m
    '> ,
    '> +
    '> 7915456
    '> −
    '> 78
    '> −
    '> 90
    '> ′
    '> ,
    '> ′
    '> Операто
    '> р
    '> ′
    '> ,
    '> ′
    '> k
    '> u
    '> z
    '> n
    '> e
    '> t
    '> s
    '> o
    '> v
    '> a
    '> e
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(4,
    '> ′
    '>  КузнецоваЕленаПавловна
    '> ′
    '>  ,
    '> ′
    '>  elena.kuznetsova@gmail.com,+7915456−78−90
    '> ′
    '>  ,
    '> ′
    '>  Оператор
    '> ′
    '>  ,
    '> ′
    '>  kuznetsova
    '> e
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123'),
    -> (5, 'Морозов Роман Николаевич', 'roman.morozov@company.org, +7 909 567-89-01', 'Администратор', 'morozov_r', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 6
    '> ,
    '> ′
    '> ВолковаТатьянаЛеонидовн
    '> а
    '> ′
    '> ,
    '> ′
    '> t
    '> a
    '> t
    '> y
    '> a
    '> n
    '> a
    '> .
    '> v
    '> o
    '> l
    '> k
    '> o
    '> v
    '> a
    '> @
    '> e
    '> x
    '> a
    '> m
    '> p
    '> l
    '> e
    '> .
    '> n
    '> e
    '> t
    '> ,
    '> +
    '> 7925678
    '> −
    '> 90
    '> −
    '> 12
    '> ′
    '> ,
    '> ′
    '> Операто
    '> р
    '> ′
    '> ,
    '> ′
    '> v
    '> o
    '> l
    '> k
    '> o
    '> v
    '> a
    '> t
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(6,
    '> ′
    '>  ВолковаТатьянаЛеонидовна
    '> ′
    '>  ,
    '> ′
    '>  tatyana.volkova@example.net,+7925678−90−12
    '> ′
    '>  ,
    '> ′
    '>  Оператор
    '> ′
    '>  ,
    '> ′
    '>  volkova
    '> t
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123'),
    -> (7, 'Алексеев Сергей Михайлович', 'sergey.alekseev@biz.ru, +7 910 789-01-23', 'Оператор', 'alekseev_s', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 8
    '> ,
    '> ′
    '> НикитинаОльгаАлександровн
    '> а
    '> ′
    '> ,
    '> ′
    '> o
    '> l
    '> g
    '> a
    '> .
    '> n
    '> i
    '> k
    '> i
    '> t
    '> i
    '> n
    '> a
    '> @
    '> p
    '> r
    '> o
    '> t
    '> o
    '> n
    '> .
    '> m
    '> e
    '> ,
    '> +
    '> 7905890
    '> −
    '> 12
    '> −
    '> 34
    '> ′
    '> ,
    '> ′
    '> Операто
    '> р
    '> ′
    '> ,
    '> ′
    '> n
    '> i
    '> k
    '> i
    '> t
    '> i
    '> n
    '> a
    '> o
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(8,
    '> ′
    '>  НикитинаОльгаАлександровна
    '> ′
    '>  ,
    '> ′
    '>  olga.nikitina@proton.me,+7905890−12−34
    '> ′
    '>  ,
    '> ′
    '>  Оператор
    '> ′
    '>  ,
    '> ′
    '>  nikitina
    '> o
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123'),
    -> (9, 'Фёдоров Игорь Борисович', 'igor.fedorov@outlook.com, +7 927 901-23-45', 'Администратор', 'fedorov_i', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 10
    '> ,
    '> ′
    '> ГригорьеваНатальяКонстантиновн
    '> а
    '> ′
    '> ,
    '> ′
    '> n
    '> a
    '> t
    '> a
    '> l
    '> i
    '> a
    '> .
    '> g
    '> r
    '> i
    '> g
    '> o
    '> r
    '> i
    '> e
    '> v
    '> a
    '> @
    '> m
    '> a
    '> i
    '> l
    '> .
    '> c
    '> o
    '> m
    '> ,
    '> +
    '> 7901012
    '> −
    '> 34
    '> −
    '> 56
    '> ′
    '> ,
    '> ′
    '> Администрато
    '> р
    '> ′
    '> ,
    '> ′
    '> g
    '> r
    '> i
    '> g
    '> o
    '> r
    '> i
    '> e
    '> v
    '> a
    '> n
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(10,
    '> ′
    '>  ГригорьеваНатальяКонстантиновна
    '> ′
    '>  ,
    '> ′
    '>  natalia.grigorieva@mail.com,+7901012−34−56
    '> ′
    '>  ,
    '> ′
    '>  Администратор
    '> ′
    '>  ,
    '> ′
    '>  grigorieva
    '> n
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123');
ERROR 1406 (22001): Data too long for column 'PasswordHash' at row 1
mysql>
mysql> VALUES
    -> (1, 'Иванов Алексей Петрович', 'alex.ivanov@example.com, +7 916 123-45-67', 'Администратор', 'ivanov_a', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 2
    '> ,
    '> ′
    '> ПетроваМарияИвановн
    '> а
    '> ′
    '> ,
    '> ′
    '> m
    '> a
    '> r
    '> i
    '> a
    '> .
    '> p
    '> e
    '> t
    '> r
    '> o
    '> v
    '> a
    '> @
    '> m
    '> a
    '> i
    '> l
    '> .
    '> r
    '> u
    '> ,
    '> +
    '> 7903234
    '> −
    '> 56
    '> −
    '> 78
    '> ′
    '> ,
    '> ′
    '> Операто
    '> р
    '> ′
    '> ,
    '> ′
    '> p
    '> e
    '> t
    '> r
    '> o
    '> v
    '> a
    '> m
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(2,
    '> ′
    '>  ПетроваМарияИвановна
    '> ′
    '>  ,
    '> ′
    '>  maria.petrova@mail.ru,+7903234−56−78
    '> ′
    '>  ,
    '> ′
    '>  Оператор
    '> ′
    '>  ,
    '> ′
    '>  petrova
    '> m
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123'),
    -> (3, 'Сидоров Дмитрий Викторович', 'dmitry.sidorov@yandex.ru, +7 926 345-67-89', 'Оператор', 'sidorov_d', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 4
    '> ,
    '> ′
    '> КузнецоваЕленаПавловн
    '> а
    '> ′
    '> ,
    '> ′
    '> e
    '> l
    '> e
    '> n
    '> a
    '> .
    '> k
    '> u
    '> z
    '> n
    '> e
    '> t
    '> s
    '> o
    '> v
    '> a
    '> @
    '> g
    '> m
    '> a
    '> i
    '> l
    '> .
    '> c
    '> o
    '> m
    '> ,
    '> +
    '> 7915456
    '> −
    '> 78
    '> −
    '> 90
    '> ′
    '> ,
    '> ′
    '> Операто
    '> р
    '> ′
    '> ,
    '> ′
    '> k
    '> u
    '> z
    '> n
    '> e
    '> t
    '> s
    '> o
    '> v
    '> a
    '> e
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(4,
    '> ′
    '>  КузнецоваЕленаПавловна
    '> ′
    '>  ,
    '> ′
    '>  elena.kuznetsova@gmail.com,+7915456−78−90
    '> ′
    '>  ,
    '> ′
    '>  Оператор
    '> ′
    '>  ,
    '> ′
    '>  kuznetsova
    '> e
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123'),
    -> (5, 'Морозов Роман Николаевич', 'roman.morozov@company.org, +7 909 567-89-01', 'Администратор', 'morozov_r', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 6
    '> ,
    '> ′
    '> ВолковаТатьянаЛеонидовн
    '> а
    '> ′
    '> ,
    '> ′
    '> t
    '> a
    '> t
    '> y
    '> a
    '> n
    '> a
    '> .
    '> v
    '> o
    '> l
    '> k
    '> o
    '> v
    '> a
    '> @
    '> e
    '> x
    '> a
    '> m
    '> p
    '> l
    '> e
    '> .
    '> n
    '> e
    '> t
    '> ,
    '> +
    '> 7925678
    '> −
    '> 90
    '> −
    '> 12
    '> ′
    '> ,
    '> ′
    '> Операто
    '> р
    '> ′
    '> ,
    '> ′
    '> v
    '> o
    '> l
    '> k
    '> o
    '> v
    '> a
    '> t
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(6,
    '> ′
    '>  ВолковаТатьянаЛеонидовна
    '> ′
    '>  ,
    '> ′
    '>  tatyana.volkova@example.net,+7925678−90−12
    '> ′
    '>  ,
    '> ′
    '>  Оператор
    '> ′
    '>  ,
    '> ′
    '>  volkova
    '> t
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123'),
    -> (7, 'Алексеев Сергей Михайлович', 'sergey.alekseev@biz.ru, +7 910 789-01-23', 'Оператор', 'alekseev_s', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 8
    '> ,
    '> ′
    '> НикитинаОльгаАлександровн
    '> а
    '> ′
    '> ,
    '> ′
    '> o
    '> l
    '> g
    '> a
    '> .
    '> n
    '> i
    '> k
    '> i
    '> t
    '> i
    '> n
    '> a
    '> @
    '> p
    '> r
    '> o
    '> t
    '> o
    '> n
    '> .
    '> m
    '> e
    '> ,
    '> +
    '> 7905890
    '> −
    '> 12
    '> −
    '> 34
    '> ′
    '> ,
    '> ′
    '> Операто
    '> р
    '> ′
    '> ,
    '> ′
    '> n
    '> i
    '> k
    '> i
    '> t
    '> i
    '> n
    '> a
    '> o
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(8,
    '> ′
    '>  НикитинаОльгаАлександровна
    '> ′
    '>  ,
    '> ′
    '>  olga.nikitina@proton.me,+7905890−12−34
    '> ′
    '>  ,
    '> ′
    '>  Оператор
    '> ′
    '>  ,
    '> ′
    '>  nikitina
    '> o
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123'),
    -> (9, 'Фёдоров Игорь Борисович', 'igor.fedorov@outlook.com, +7 927 901-23-45', 'Администратор', 'fedorov_i', '
    '> 2
    '> b
    '> 2b10
    '> d
    '> u
    '> m
    '> m
    '> y
    '> h
    '> a
    '> s
    '> h
    '> f
    '> o
    '> r
    '> 123
    '> ′
    '> )
    '> ,
    '> (
    '> 10
    '> ,
    '> ′
    '> ГригорьеваНатальяКонстантиновн
    '> а
    '> ′
    '> ,
    '> ′
    '> n
    '> a
    '> t
    '> a
    '> l
    '> i
    '> a
    '> .
    '> g
    '> r
    '> i
    '> g
    '> o
    '> r
    '> i
    '> e
    '> v
    '> a
    '> @
    '> m
    '> a
    '> i
    '> l
    '> .
    '> c
    '> o
    '> m
    '> ,
    '> +
    '> 7901012
    '> −
    '> 34
    '> −
    '> 56
    '> ′
    '> ,
    '> ′
    '> Администрато
    '> р
    '> ′
    '> ,
    '> ′
    '> g
    '> r
    '> i
    '> g
    '> o
    '> r
    '> i
    '> e
    '> v
    '> a
    '> n
    '> ′
    '> ,
    '> ′
    '> dummyhashfor123
    '> ′
    '>  ),(10,
    '> ′
    '>  ГригорьеваНатальяКонстантиновна
    '> ′
    '>  ,
    '> ′
    '>  natalia.grigorieva@mail.com,+7901012−34−56
    '> ′
    '>  ,
    '> ′
    '>  Администратор
    '> ′
    '>  ,
    '> ′
    '>  grigorieva
    '> n
    '> ′
    '> ​
    '>  ,
    '> ′
    '>  2b
    '> 10
    '> 10dummyhashfor123');
ERROR 1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '(1, 'Иванов Алексей Петрович', 'alex.ivanov@example.com, +7' at line 2
mysql>
mysql> SET FOREIGN_KEY_CHECKS = 1;
Query OK, 0 rows affected (0.003 sec)

mysql> SELECT COUNT(*) FROM Users;
+----------+
| COUNT(*) |
+----------+
|        0 |
+----------+
1 row in set (0.008 sec)

mysql> mkdir vending-api
    -> cd vending-api
    -> npm init -y
    -> npm install express mysql2 cors bcryptjs jsonwebtoken dotenv
    ->
    -> USE vending_network;
ERROR 1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 'mkdir vending-api
cd vending-api
npm init -y
npm install express mysql2 cors bcr' at line 1
mysql> SELECT * FROM VendingMachines\G
*************************** 1. row ***************************
               MachineID: 1
                Location: г. Санкт?Петербург, Невский пр., д. 50, ТЦ ?Галерея?, 2?й этаж.
                   Model: VendCore X-200
             PaymentType: с оплатой картой
              FullIncome: 1250000.00
            SerialNumber: SC123456789
         InventoryNumber: INV-2025-001
            Manufacturer: ООО ?ВендТех?
         ManufactureDate: 2025-05-01
     DateOfCommissioning: 2025-05-10
    LastVerificationDate: 2025-06-15
    VerificationInterval: 6
           ResourceHours: 2500
        DateOfNextFixing: 2026-08-01
    MaintenanceTimeHours: 4
           MachineStatus: Работает
                 Country: Россия
           InventoryDate: 2025-07-20
       LastCheckedByUser: Иванов А. С.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
*************************** 2. row ***************************
               MachineID: 2
                Location: Московская обл., г. Химки, ул. Московская, д. 15, офис 301.
                   Model: CoffeeMaster Pro 500
             PaymentType: с оплатой наличными
              FullIncome: 1250000.00
            SerialNumber: SN987654321
         InventoryNumber: INV-2025-002
            Manufacturer: АО ?КофеМаш?
         ManufactureDate: 2025-06-15
     DateOfCommissioning: 2025-06-20
    LastVerificationDate: 2025-07-25
    VerificationInterval: 12
           ResourceHours: 1800
        DateOfNextFixing: 2026-09-10
    MaintenanceTimeHours: 8
           MachineStatus: Вышел из строя
                 Country: Китай
           InventoryDate: 2025-08-15
       LastCheckedByUser: Петрова М. И.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
*************************** 3. row ***************************
               MachineID: 3
                Location: г. Казань, ул. Баумана, д. 20, кафетерий
                   Model: SnackVend S-300
             PaymentType: два вида оплаты
              FullIncome: 1250000.00
            SerialNumber: VCX200-001
         InventoryNumber: INV-2025-003
            Manufacturer: ЗАО ?СнекВенд?
         ManufactureDate: 2025-07-20
     DateOfCommissioning: 2025-07-22
    LastVerificationDate: 2025-08-05
    VerificationInterval: 24
           ResourceHours: 1801
        DateOfNextFixing: 2026-10-20
    MaintenanceTimeHours: 12
           MachineStatus: В ремонте/на обслуживании
                 Country: Германия
           InventoryDate: 2025-09-10
       LastCheckedByUser: Сидоров Д. В.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
*************************** 4. row ***************************
               MachineID: 4
                Location: г. Екатеринбург, ул. Ленина, д. 50, холл бизнес?центра.
                   Model: AquaVend Water 2025
             PaymentType: с оплатой картой
              FullIncome: 1250000.00
            SerialNumber: CM500-PRO-002
         InventoryNumber: INV-2025-004
            Manufacturer: ООО ?АкваВенд?
         ManufactureDate: 2025-08-10
     DateOfCommissioning: 2025-08-15
    LastVerificationDate: 2025-09-20
    VerificationInterval: 18
           ResourceHours: 1802
        DateOfNextFixing: 2026-11-05
    MaintenanceTimeHours: 6
           MachineStatus: Работает
                 Country: Южная Корея
           InventoryDate: 2025-09-10
       LastCheckedByUser: Кузнецова Е. П.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
*************************** 5. row ***************************
               MachineID: 5
                Location: г. Новосибирск, Красный пр., д. 100, университетский кампус.
                   Model: VendoTech Elite 400
             PaymentType: два вида оплаты
              FullIncome: 1250000.00
            SerialNumber: SV300-SN003
         InventoryNumber: INV-2025-005
            Manufacturer: ООО ?ТехноВенд?
         ManufactureDate: 2025-09-25
     DateOfCommissioning: 2025-09-30
    LastVerificationDate: 2025-10-01
    VerificationInterval: 36
           ResourceHours: 1803
        DateOfNextFixing: 2026-12-15
    MaintenanceTimeHours: 16
           MachineStatus: В ремонте/на обслуживании
                 Country: США
           InventoryDate: 2025-11-10
       LastCheckedByUser: Морозов Р. Н.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
*************************** 6. row ***************************
               MachineID: 6
                Location: г. Сочи, Курортный пр., д. 70, отель ?Морская звезда?, лобби.
                   Model: QuickBite Mini 100
             PaymentType: с оплатой наличными
              FullIncome: 1250000.00
            SerialNumber: QB100-MIN-006
         InventoryNumber: INV-2025-006
            Manufacturer: ИП ?МиниВенд?
         ManufactureDate: 2025-10-05
     DateOfCommissioning: 2025-10-10
    LastVerificationDate: 2025-11-15
    VerificationInterval: 12
           ResourceHours: 1804
        DateOfNextFixing: 2026-03-25
    MaintenanceTimeHours: 10
           MachineStatus: Вышел из строя
                 Country: Италия
           InventoryDate: 2025-12-10
       LastCheckedByUser: Волкова Т. Л.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
*************************** 7. row ***************************
               MachineID: 7
                Location: г. Нижний Новгород, ул. Большая Покровская, д. 40, торговый пассаж.
                   Model: HotDrink Station 600
             PaymentType: с оплатой картой
              FullIncome: 1250000.00
            SerialNumber: VT400-ELT-005
         InventoryNumber: INV-2025-007
            Manufacturer: ООО ?ГорячийНапиток?
         ManufactureDate: 2025-11-12
     DateOfCommissioning: 2025-11-15
    LastVerificationDate: 2025-12-22
    VerificationInterval: 6
           ResourceHours: 1805
        DateOfNextFixing: 2026-03-03
    MaintenanceTimeHours: 3
           MachineStatus: Работает
                 Country: Турция
           InventoryDate: 2025-12-10
       LastCheckedByUser: Алексеев С. М.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
*************************** 8. row ***************************
               MachineID: 8
                Location: г. Самара, ул. Молодогвардейская, д. 120, ТЦ ?Мега?.
                   Model: FreshFood Vend 700
             PaymentType: два вида оплаты
              FullIncome: 1250000.00
            SerialNumber: FF700-VND-008
         InventoryNumber: INV-2025-008
            Manufacturer: АО ?ФрэшФудВенд?
         ManufactureDate: 2025-12-18
     DateOfCommissioning: 2025-12-20
    LastVerificationDate: 2026-01-05
    VerificationInterval: 24
           ResourceHours: 1806
        DateOfNextFixing: 2026-04-12
    MaintenanceTimeHours: 18
           MachineStatus: В ремонте/на обслуживании
                 Country: Япония
           InventoryDate: 2026-01-10
       LastCheckedByUser: Никитина О. А.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
*************************** 9. row ***************************
               MachineID: 9
                Location: г. Ростов?на?Дону, ул. Садовая, д. 80, административное здание.
                   Model: IceCream Vend 250
             PaymentType: с оплатой наличными
              FullIncome: 1250000.00
            SerialNumber: HDS600-007
         InventoryNumber: INV-2025-009
            Manufacturer: ООО ?АйсВенд?
         ManufactureDate: 2026-01-03
     DateOfCommissioning: 2026-01-10
    LastVerificationDate: 2026-01-12
    VerificationInterval: 18
           ResourceHours: 1807
        DateOfNextFixing: 2026-05-22
    MaintenanceTimeHours: 7
           MachineStatus: Вышел из строя
                 Country: Польша
           InventoryDate: 2026-01-13
       LastCheckedByUser: Фёдоров И. Б.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
*************************** 10. row ***************************
               MachineID: 10
                Location: г. Владивосток, ул. Светланская, д. 60, морской вокзал.
                   Model: Print&Go Kiosk 150
             PaymentType: с оплатой картой
              FullIncome: 1250000.00
            SerialNumber: PK150-010
         InventoryNumber: INV-2025-010
            Manufacturer: ЗАО ?ПринтВенд?
         ManufactureDate: 2026-01-14
     DateOfCommissioning: 2026-01-20
    LastVerificationDate: 2026-01-25
    VerificationInterval: 12
           ResourceHours: 1808
        DateOfNextFixing: 2026-07-02
    MaintenanceTimeHours: 14
           MachineStatus: Работает
                 Country: Тайвань
           InventoryDate: 2026-01-22
       LastCheckedByUser: Григорьева Н. К.
NextScheduledMaintenance: NULL
                 ModemID: -1
                 Company: Франчайзи
10 rows in set (0.108 sec)

mysql> SELECT * FROM Products\G
*************************** 1. row ***************************
       ProductID: 1
            Name: Кофе ?Эспрессо?
     Description: Эспрессо из 100% арабики, без добавок. Объём: 250 мл
           Price: 120.00
         InStock: 18
        MinStock: 5
PropensityToSell: 3.50
*************************** 2. row ***************************
       ProductID: 2
            Name: Чипсы ?Сыр и Лук?
     Description: Картофельные чипсы с ароматом сыра и лука. Без ГМО
           Price: 95.00
         InStock: 25
        MinStock: 8
PropensityToSell: 2.10
*************************** 3. row ***************************
       ProductID: 3
            Name: Вода минеральная негазированная
     Description: Природная минеральная вода, низкоминерализованная. Без газа
           Price: 60.00
         InStock: 40
        MinStock: 10
PropensityToSell: 4.80
*************************** 4. row ***************************
       ProductID: 4
            Name: Шоколадный батончик ?Ореховый восторг?
     Description: Молочный шоколад с цельным фундуком и карамельной начинкой
           Price: 85.00
         InStock: 30
        MinStock: 7
PropensityToSell: 1.90
*************************** 5. row ***************************
       ProductID: 5
            Name: Газированный напиток ?Кола?
     Description: Газированный напиток со вкусом колы, с кофеином
           Price: 75.00
         InStock: 22
        MinStock: 6
PropensityToSell: 2.70
*************************** 6. row ***************************
       ProductID: 6
            Name: Смесь орехов ?Классика?
     Description: Смесь миндаля, фундука и грецкого ореха, слегка подсоленная
           Price: 150.00
         InStock: 15
        MinStock: 4
PropensityToSell: 1.20
*************************** 7. row ***************************
       ProductID: 7
            Name: Леденцы ?Мятные?
     Description: Мятные леденцы без сахара, с натуральным ароматизатором
           Price: 45.00
         InStock: 50
        MinStock: 12
PropensityToSell: 5.30
*************************** 8. row ***************************
       ProductID: 8
            Name: Попкорн ?Сливочный?
     Description: Воздушный попкорн со сливочным маслом и солью
           Price: 70.00
         InStock: 28
        MinStock: 9
PropensityToSell: 1.80
*************************** 9. row ***************************
       ProductID: 9
            Name: Энергетический напиток ?Turbo?
     Description: Энергетический напиток с таурином, кофеином и витаминами группы B
           Price: 130.00
         InStock: 12
        MinStock: 3
PropensityToSell: 2.40
9 rows in set (0.011 sec)

mysql> SELECT * FROM Sales\G
*************************** 1. row ***************************
      SaleID: 1
   MachineID: 2
   ProductID: 9
    Quantity: 1
     SaleSum: 120.00
SaleDateTime: 2026-01-22 08:15:30
 PaymentType: Карта
*************************** 2. row ***************************
      SaleID: 2
   MachineID: 5
   ProductID: 2
    Quantity: 3
     SaleSum: 285.00
SaleDateTime: 2026-01-22 10:45:12
 PaymentType: Наличные
*************************** 3. row ***************************
      SaleID: 3
   MachineID: 9
   ProductID: 6
    Quantity: 2
     SaleSum: 300.00
SaleDateTime: 2026-01-22 12:30:45
 PaymentType: QR-код
*************************** 4. row ***************************
      SaleID: 4
   MachineID: 8
   ProductID: 5
    Quantity: 1
     SaleSum: 85.00
SaleDateTime: 2026-01-22 14:20:05
 PaymentType: Карта
*************************** 5. row ***************************
      SaleID: 5
   MachineID: 6
   ProductID: 7
    Quantity: 4
     SaleSum: 180.00
SaleDateTime: 2026-01-22 16:55:22
 PaymentType: Наличные
*************************** 6. row ***************************
      SaleID: 6
   MachineID: 1
   ProductID: 1
    Quantity: 1
     SaleSum: 120.00
SaleDateTime: 2026-01-22 18:03:17
 PaymentType: QR-код
*************************** 7. row ***************************
      SaleID: 7
   MachineID: 3
   ProductID: 4
    Quantity: 5
     SaleSum: 425.00
SaleDateTime: 2026-01-22 19:40:50
 PaymentType: Карта
*************************** 8. row ***************************
      SaleID: 8
   MachineID: 10
   ProductID: 2
    Quantity: 2
     SaleSum: 190.00
SaleDateTime: 2026-01-22 21:10:33
 PaymentType: Наличные
*************************** 9. row ***************************
      SaleID: 9
   MachineID: 4
   ProductID: 8
    Quantity: 1
     SaleSum: 70.00
SaleDateTime: 2026-01-22 22:50:47
 PaymentType: QR-код
*************************** 10. row ***************************
      SaleID: 10
   MachineID: 7
   ProductID: 7
    Quantity: 3
     SaleSum: 135.00
SaleDateTime: 2026-01-22 23:59:01
 PaymentType: Карта
10 rows in set (0.008 sec)

mysql> SELECT * FROM Users\G
Empty set (0.224 sec)

mysql> ^A
