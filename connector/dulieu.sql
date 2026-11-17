-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 29, 2026 at 09:21 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */
;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */
;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */
;
/*!40101 SET NAMES utf8mb4 */
;
--
-- Database: `dulieu`
--

-- --------------------------------------------------------
--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
                         `username` varchar(255) NOT NULL,
                         `password` varchar(255) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;
--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`username`, `password`)
VALUES ('user01', '123abc'),
       ('user02', 'abc123');
-- --------------------------------------------------------
--
-- Table structure for table `nhanvien`
--

CREATE TABLE `nhanvien` (
                            `idnv` varchar(10) NOT NULL,
                            `hoten` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
                            `idpb` varchar(10) NOT NULL,
                            `diachi` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;
--
-- Dumping data for table `nhanvien`
--

INSERT INTO `nhanvien` (`idnv`, `hoten`, `idpb`, `diachi`)
VALUES ('NV005', 'Phạm Quốc Tuấn', 'PB02', 'DaNa'),
       ('NV01', 'Lê A', 'PB01', 'DaNa'),
       ('NV02', 'Trần B', 'PB02', 'QNgai'),
       ('NV03', 'Nguyễn C', 'PB03', 'GiaLai'),
       ('NV04', 'Mạc D', 'PB01', 'QTri'),
       ('NV05', 'Đinh E', 'PB02', 'HaTinh');
-- --------------------------------------------------------
--
-- Table structure for table `phongban`
--

CREATE TABLE `phongban` (
                            `idpb` varchar(10) NOT NULL,
                            `tenpb` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
                            `mota` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;
--
-- Dumping data for table `phongban`
--

INSERT INTO `phongban` (`idpb`, `tenpb`, `mota`)
VALUES ('PB01', 'Phòng ban 1', 'Tổ chức'),
       ('PB02', 'Phòng ban 2', 'Kế toán'),
       ('PB03', 'Phòng ban 3', 'Kỹ thuật');
--
-- Indexes for dumped tables
--

--
-- Indexes for table `nhanvien`
--
ALTER TABLE `nhanvien`
    ADD PRIMARY KEY (`idnv`),
    ADD KEY `idpb` (`idpb`);
--
-- Indexes for table `phongban`
--
ALTER TABLE `phongban`
    ADD PRIMARY KEY (`idpb`);
--
-- Constraints for dumped tables
--

--
-- Constraints for table `nhanvien`
--
ALTER TABLE `nhanvien`
    ADD CONSTRAINT `nhanvien_ibfk_1` FOREIGN KEY (`idpb`) REFERENCES `phongban` (`idpb`);
COMMIT;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */
;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */
;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */
;