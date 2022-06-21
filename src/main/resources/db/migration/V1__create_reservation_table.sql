CREATE TABLE IF NOT EXISTS reservations(
     id SERIAL PRIMARY KEY,
     first_name varchar NOT NULL,
     last_name varchar NOT NULL,
     email varchar NOT NULL,
     from_date timestamp,
     to_date timestamp
);