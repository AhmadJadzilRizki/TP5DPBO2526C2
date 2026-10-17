-- ============================================================
-- File   : db_people.sql
-- Isi    : database db_people dan tabel person untuk aplikasi PeopleData
-- Import : phpMyAdmin (XAMPP/Laragon) -> tab Import, atau lewat terminal:
--          mysql -u root < db_people.sql
-- Catatan: tabel person lama (jika ada) akan dihapus lalu dibuat ulang,
--          jadi import file ini juga bisa dipakai untuk mereset data.
-- ============================================================

CREATE DATABASE IF NOT EXISTS db_people
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE db_people;

DROP TABLE IF EXISTS person;

CREATE TABLE person (
    id          VARCHAR(20)  NOT NULL,              -- identitas unik (primary key)
    nama        VARCHAR(100) NOT NULL,
    tahun_lahir INT          NOT NULL,
    kategori    VARCHAR(50)  NOT NULL,
    keaktifan   INT          NOT NULL DEFAULT 50,   -- atribut baru (0-100)
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO person (id, nama, tahun_lahir, kategori, keaktifan) VALUES
    ('P001', 'Budi Santoso',   2004, 'Mahasiswa',           85),
    ('P002', 'Siti Aminah',    1985, 'Dosen',               70),
    ('P003', 'Rian Pratama',   2003, 'Asisten Praktikum',   92),
    ('P004', 'Dewi Lestari',   1990, 'Tenaga Kependidikan', 60),
    ('P005', 'Ahmad Fauzi',    1998, 'Alumni',              35),
    ('P006', 'Nadia Putri',    2005, 'Mahasiswa',           78),
    ('P007', 'Rizky Ramadhan', 1980, 'Dosen',               88),
    ('P008', 'Maya Indah',     2002, 'Mahasiswa',           55);
