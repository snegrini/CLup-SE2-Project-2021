CREATE DATABASE  IF NOT EXISTS `np_clup` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `np_clup`;
-- MySQL dump 10.13  Distrib 8.0.22, for Win64 (x86_64)
--
-- Host: localhost    Database: np_clup
-- ------------------------------------------------------
-- Server version	8.0.22

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `address_id` int NOT NULL AUTO_INCREMENT,
  `address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `street_number` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `postal_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `country` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`address_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (1,'via Paperino','15','Milano','MI','20100','Lombardia'),(2,'via Pluto','66','Lecco','LC','23900','Lombardia');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opening_hour`
--

DROP TABLE IF EXISTS `opening_hour`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opening_hour` (
  `opening_hours_id` int NOT NULL AUTO_INCREMENT,
  `from_time` time NOT NULL,
  `to_time` time NOT NULL,
  `week_day` int NOT NULL,
  `store_id` int NOT NULL,
  PRIMARY KEY (`opening_hours_id`),
  KEY `FK_opening_hour_store` (`store_id`),
  CONSTRAINT `FK_opening_hour_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opening_hour`
--

LOCK TABLES `opening_hour` WRITE;
/*!40000 ALTER TABLE `opening_hour` DISABLE KEYS */;
INSERT INTO `opening_hour` VALUES (1,'08:00:00','12:00:00',1,2),(2,'14:00:00','18:00:00',1,2),(3,'08:00:00','12:00:00',2,2),(4,'14:00:00','18:00:00',2,2),(5,'07:00:00','22:00:00',3,2),(6,'08:00:00','12:00:00',4,2),(7,'14:00:00','18:00:00',4,2),(8,'08:00:00','12:00:00',5,2),(9,'14:00:00','18:00:00',5,2),(10,'09:00:00','13:00:00',6,2),(11,'14:00:00','19:00:00',6,2),(12,'07:00:00','22:00:00',7,2),(13,'14:00:00','18:00:00',1,1),(14,'08:00:00','12:00:00',1,1),(15,'14:00:00','18:00:00',2,1),(16,'08:00:00','12:00:00',2,1),(17,'07:00:00','22:00:00',3,1),(18,'14:00:00','18:00:00',4,1),(19,'08:00:00','12:00:00',4,1),(20,'08:00:00','12:00:00',5,1),(21,'14:00:00','18:00:00',5,1),(22,'09:00:00','13:00:00',6,1),(23,'14:00:00','19:00:00',6,1),(24,'07:00:00','22:00:00',7,1);
/*!40000 ALTER TABLE `opening_hour` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store`
--

DROP TABLE IF EXISTS `store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store` (
  `store_id` int NOT NULL AUTO_INCREMENT,
  `store_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `address_id` int NOT NULL,
  `pec_email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `store_cap` int NOT NULL,
  `customers_inside` int NOT NULL,
  `default_pass_code` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `image_path` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`store_id`),
  UNIQUE KEY `pec_email` (`pec_email`),
  UNIQUE KEY `store_name` (`store_name`),
  KEY `FK_store_address` (`address_id`),
  CONSTRAINT `FK_store_address` FOREIGN KEY (`address_id`) REFERENCES `address` (`address_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store`
--

LOCK TABLES `store` WRITE;
/*!40000 ALTER TABLE `store` DISABLE KEYS */;
INSERT INTO `store` VALUES (1,'Essecorta',1,'essecorta@example.org','5556645211',60,0,'12345678','essecorta.png'),(2,'Superal',2,'superal@example.org','5552256633',20,0,'12345678','superal.png');
/*!40000 ALTER TABLE `store` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ticket`
--

DROP TABLE IF EXISTS `ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket` (
  `ticket_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `pass_code` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `pass_status` enum('VALID','USED','EXPIRED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `queue_number` int NOT NULL,
  `date` date NOT NULL,
  `arrival_time` time NOT NULL,
  `issued_at` timestamp NOT NULL,
  `store_id` int NOT NULL,
  PRIMARY KEY (`ticket_id`),
  KEY `FK_ticket_store` (`store_id`),
  CONSTRAINT `FK_ticket_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ticket`
--

LOCK TABLES `ticket` WRITE;
/*!40000 ALTER TABLE `ticket` DISABLE KEYS */;
/*!40000 ALTER TABLE `ticket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `user_code` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `role` enum('ADMIN','MANAGER','EMPLOYEE') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `store_id` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_code` (`user_code`),
  KEY `FK_user_store` (`store_id`),
  CONSTRAINT `FK_user_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'555001','$2a$10$zW3PseEo0FIAwS2N6rjfv.8qsHnw1B.ic1TVJfdDKpYv39Eerg1N6','ADMIN',NULL),(2,'000001','$2a$10$zW3PseEo0FIAwS2N6rjfv.8qsHnw1B.ic1TVJfdDKpYv39Eerg1N6','MANAGER',1),(3,'222001','$2a$10$zW3PseEo0FIAwS2N6rjfv.8qsHnw1B.ic1TVJfdDKpYv39Eerg1N6','EMPLOYEE',1),(4,'000002','$2a$10$zW3PseEo0FIAwS2N6rjfv.8qsHnw1B.ic1TVJfdDKpYv39Eerg1N6','MANAGER',2),(5,'222002','$2a$10$zW3PseEo0FIAwS2N6rjfv.8qsHnw1B.ic1TVJfdDKpYv39Eerg1N6','EMPLOYEE',2);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-02-06 17:33:03
