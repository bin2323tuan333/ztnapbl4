-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: ztna_db
-- ------------------------------------------------------
-- Server version	9.7.0
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */
;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */
;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */
;
/*!50503 SET NAMES utf8mb4 */
;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */
;
/*!40103 SET TIME_ZONE='+00:00' */
;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */
;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */
;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */
;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */
;
SET
@MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET
@@SESSION.SQL_LOG_BIN = 0;
--
-- GTID state at the beginning of the backup
--
SET
@@GLOBAL.GTID_PURGED =
  /*!80000 '+'*/
  'de817feb-bfc8-11f1-aecb-5af3f535616a:1-1684';
--
-- Table structure for table `access_logs`
--
DROP TABLE IF EXISTS `access_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `access_logs`
(
    `id`             binary(16) NOT NULL,
    `connector_id`   binary(16) DEFAULT NULL,
    `connector_name` varchar(255) DEFAULT NULL,
    `created_at`     datetime(6) NOT NULL,
    `decision`       enum('ALLOW', 'DENY') NOT NULL,
    `reason`         varchar(255) NOT NULL,
    `resource_host`  varchar(100) DEFAULT NULL,
    `resource_id`    binary(16) DEFAULT NULL,
    `resource_name`  varchar(100) DEFAULT NULL,
    `user_id`        binary(16) DEFAULT NULL,
    `user_ip`        varchar(255) NOT NULL,
    `user_name`      varchar(100) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `audit_logs`
--
DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `audit_logs`
(
    `id`             binary(16) NOT NULL,
    `action`         enum(
    'ASSIGN_RESOURCE_TO_RULE',
    'ASSIGN_USER_TO_RULE',
    'BLOCK_USER',
    'CONNECTOR_OFFLINE',
    'CONNECTOR_ONLINE',
    'CREATE_CONNECTOR',
    'CREATE_RESOURCE',
    'CREATE_RULE',
    'CREATE_USER',
    'DELETE_CONNECTOR',
    'DELETE_RESOURCE',
    'DELETE_RULE',
    'DELETE_USER',
    'DISABLE_CONNECTOR',
    'DISABLE_RESOURCE',
    'DISABLE_RULE',
    'ENABLE_CONNECTOR',
    'ENABLE_RESOURCE',
    'ENABLE_RULE',
    'LOGIN',
    'LOGIN_FAILED',
    'LOGOUT',
    'REFRESH_TOKEN',
    'RESET_PASSWORD',
    'UNBLOCK_USER',
    'UPDATE_CONNECTOR',
    'UPDATE_RESOURCE',
    'UPDATE_RULE',
    'UPDATE_USER'
  ) NOT NULL,
    `actor_id`       binary(16) NOT NULL,
    `actor_username` varchar(100) NOT NULL,
    `created_at`     datetime(6) NOT NULL,
    `details`        text,
    `ip_address`     varchar(45) DEFAULT NULL,
    `target_id`      varchar(36) DEFAULT NULL,
    `target_type`    enum(
    'CONNECTOR',
    'PERMISSION',
    'RESOURCE',
    'SESSION',
    'SYSTEM',
    'USER'
  ) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `connectors`
--
DROP TABLE IF EXISTS `connectors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `connectors`
(
    `id`                binary(16) NOT NULL,
    `connection_string` text         NOT NULL,
    `created_at`        datetime(6) NOT NULL,
    `enabled`           bit(1)       NOT NULL,
    `name`              varchar(100) NOT NULL,
    `updated_at`        datetime(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKeen09jq3m3ybekc7oosu7oxkh` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `refresh_tokens`
--
DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `refresh_tokens`
(
    `id`         binary(16) NOT NULL,
    `active`     bit(1)        NOT NULL,
    `expired_at` datetime(6) NOT NULL,
    `token`      varchar(1000) NOT NULL,
    `user_id`    binary(16) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK7tdcd6ab5wsgoudnvj7xf1b7l` (`user_id`),
    CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `resources`
--
DROP TABLE IF EXISTS `resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `resources`
(
    `id`           binary(16) NOT NULL,
    `created_at`   datetime(6) NOT NULL,
    `description`  varchar(2000) DEFAULT NULL,
    `enabled`      bit(1)       NOT NULL,
    `host`         varchar(255) NOT NULL,
    `name`         varchar(100) NOT NULL,
    `port`         int          NOT NULL,
    `type`         enum('MYSQL', 'SSH', 'WEB') NOT NULL,
    `updated_at`   datetime(6) NOT NULL,
    `connector_id` binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKl85pqajoc7v2drqv3tj3rcmpq` (`name`),
    KEY            `FKbyx5kfselgbi5wq1a94iixux5` (`connector_id`),
    CONSTRAINT `FKbyx5kfselgbi5wq1a94iixux5` FOREIGN KEY (`connector_id`) REFERENCES `connectors` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `rule_resources`
--
DROP TABLE IF EXISTS `rule_resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `rule_resources`
(
    `rule_id`     binary(16) NOT NULL,
    `resource_id` binary(16) NOT NULL,
    PRIMARY KEY (`rule_id`, `resource_id`),
    KEY           `FKeqikp9siigjgmnmddgec2teay` (`resource_id`),
    CONSTRAINT `FK3om4ewqjh9syfr3n9k0nsdsay` FOREIGN KEY (`rule_id`) REFERENCES `rules` (`id`),
    CONSTRAINT `FKeqikp9siigjgmnmddgec2teay` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `rule_users`
--
DROP TABLE IF EXISTS `rule_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `rule_users`
(
    `rule_id` binary(16) NOT NULL,
    `user_id` binary(16) NOT NULL,
    PRIMARY KEY (`rule_id`, `user_id`),
    KEY       `FK6rofroem5fb01crtr84k4w2to` (`user_id`),
    CONSTRAINT `FK6rofroem5fb01crtr84k4w2to` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FKgcrixlq3kudr3sfy4137t2swr` FOREIGN KEY (`rule_id`) REFERENCES `rules` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `rules`
--
DROP TABLE IF EXISTS `rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `rules`
(
    `id`          binary(16) NOT NULL,
    `created_at`  datetime(6) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `enabled`     bit(1)       NOT NULL,
    `expires_at`  datetime(6) DEFAULT NULL,
    `name`        varchar(100) NOT NULL,
    `updated_at`  datetime(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKasd57j4i2nr2043y2jvq4m1fh` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Table structure for table `users`
--
DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `users`
(
    `id`            binary(16) NOT NULL,
    `created_at`    datetime(6) NOT NULL,
    `email`         varchar(255) DEFAULT NULL,
    `enabled`       bit(1)       NOT NULL,
    `full_name`     varchar(255) NOT NULL,
    `last_login_at` datetime(6) DEFAULT NULL,
    `password`      varchar(255) NOT NULL,
    `role`          enum('ADMIN', 'SUPERVISOR', 'USER') NOT NULL,
    `updated_at`    datetime(6) NOT NULL,
    `username`      varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
    UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;
--
-- Dumping routines for database 'ztna_db'
--
SET
@@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */
;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */
;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */
;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */
;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */
;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */
;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */
;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */
;
-- Dump completed on 2026-11-17 10:05:31