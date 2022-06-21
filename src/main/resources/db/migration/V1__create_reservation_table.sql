CREATE TABLE IF NOT EXISTS reservations(
 `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `first_name` varchar(20),
 `last_name` varchar(20),
 `email` varchar(50),
 `from_date` timestamp,
 `to_date` timestamp
);