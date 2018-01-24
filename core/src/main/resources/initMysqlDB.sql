

/*!40101  SET character_set_client = utf8 */;

SET foreign_key_checks=0;


--
-- Table structure for table `additionalmetadatatable`
--
DROP TABLE IF EXISTS `additionalmetadatatable`;
CREATE TABLE `additionalmetadatatable` (
  `value` text,
  `key` varchar(255) NOT NULL,
  `id` varchar(60) NOT NULL,
  PRIMARY KEY (`id`,`key`),
  CONSTRAINT `"additionalmetadatatable_fk"` FOREIGN KEY (`id`) REFERENCES `metadata` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `commenttable`
--
DROP TABLE IF EXISTS `commenttable`;
CREATE TABLE `commenttable` (
  `commentId` varchar(255) DEFAULT NULL,
  `comment` text,
  `ID` varchar(255) DEFAULT NULL,
  `commentowner` varchar(255) DEFAULT NULL,
  `time` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `data`
--
DROP TABLE IF EXISTS `data`;
CREATE TABLE `data` (
  `owner` varchar(255) DEFAULT NULL,
  `data` longblob,
  `metadataId` varchar(45) DEFAULT NULL,
  `id` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `metadata`
--
DROP TABLE IF EXISTS `metadata`;
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
