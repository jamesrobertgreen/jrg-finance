-- MySQL dump 10.13  Distrib 5.7.17, for macos10.12 (x86_64)
--
-- Host: 127.0.0.1    Database: adcma
-- ------------------------------------------------------
-- Server version	5.7.18

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `additionalmetadatatable`
--

DROP TABLE IF EXISTS `additionalmetadatatable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `additionalmetadatatable` (
  `value` text,
  `key` varchar(255) NOT NULL,
  `id` varchar(60) NOT NULL,
  PRIMARY KEY (`id`,`key`),
  CONSTRAINT `‘additionalmetadatatable_fk’` FOREIGN KEY (`id`) REFERENCES `metadata` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `additionalmetadatatable`
--

LOCK TABLES `additionalmetadatatable` WRITE;
/*!40000 ALTER TABLE `additionalmetadatatable` DISABLE KEYS */;
/*!40000 ALTER TABLE `additionalmetadatatable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `commenttable`
--

DROP TABLE IF EXISTS `commenttable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `commenttable` (
  `commentId` varchar(255) DEFAULT NULL,
  `comment` text,
  `ID` varchar(255) DEFAULT NULL,
  `commentowner` varchar(255) DEFAULT NULL,
  `time` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `commenttable`
--

LOCK TABLES `commenttable` WRITE;
/*!40000 ALTER TABLE `commenttable` DISABLE KEYS */;
/*!40000 ALTER TABLE `commenttable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data`
--

DROP TABLE IF EXISTS `data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data` (
  `owner` varchar(255) DEFAULT NULL,
  `data` longblob,
  `metadataId` varchar(45) DEFAULT NULL,
  `id` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data`
--

LOCK TABLES `data` WRITE;
/*!40000 ALTER TABLE `data` DISABLE KEYS */;
/*!40000 ALTER TABLE `data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metadata`
--

DROP TABLE IF EXISTS `metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata` (
  `formPath` varchar(1000) DEFAULT NULL,
  `formType` varchar(100) DEFAULT NULL,
  `description` text,
  `formName` varchar(255) DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `enableAnonymousSave` varchar(45) DEFAULT NULL,
  `renderPath` varchar(1000) DEFAULT NULL,
  `nodeType` varchar(45) DEFAULT NULL,
  `charset` varchar(45) DEFAULT NULL,
  `userdataID` varchar(100) DEFAULT NULL,
  `status` varchar(45) DEFAULT NULL,
  `formmodel` varchar(45) DEFAULT NULL,
  `markedForDeletion` varchar(45) DEFAULT NULL,
  `showDorClass` varchar(255) DEFAULT NULL,
  `sling:resourceType` varchar(1000) DEFAULT NULL,
  `attachmentList` longtext,
  `draftID` varchar(45) DEFAULT NULL,
  `submitID` varchar(45) DEFAULT NULL,
  `id` varchar(60) NOT NULL,
  `profile` varchar(255) DEFAULT NULL,
  `submitUrl` varchar(1000) DEFAULT NULL,
  `xdpRef` varchar(1000) DEFAULT NULL,
  `agreementId` varchar(255) DEFAULT NULL,
  `nextSigners` varchar(255) DEFAULT NULL,
  `eSignStatus` varchar(45) DEFAULT NULL,
  `pendingSignID` varchar(45) DEFAULT NULL,
  `agreementDataId` varchar(255) DEFAULT NULL,
  `enablePortalSubmit` varchar(45) DEFAULT NULL,
  `submitType` varchar(45) DEFAULT NULL,
  `dataType` varchar(45) DEFAULT NULL,
  `jcr:lastModified` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ID_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metadata`
--

LOCK TABLES `metadata` WRITE;
/*!40000 ALTER TABLE `metadata` DISABLE KEYS */;
/*!40000 ALTER TABLE `metadata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `survey`
--

DROP TABLE IF EXISTS `survey`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `survey` (
  `id` int(11) NOT NULL,
  `surveyId` varchar(36) NOT NULL,
  `jsonData` json NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `surveyId_UNIQUE` (`surveyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `survey`
--

LOCK TABLES `survey` WRITE;
/*!40000 ALTER TABLE `survey` DISABLE KEYS */;
/*!40000 ALTER TABLE `survey` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-01-23  0:30:31
