-- MySQL dump 10.13  Distrib 8.0.29, for macos12 (arm64)
--
-- Host: 127.0.0.1    Database: flow_job
-- ------------------------------------------------------
-- Server version	8.0.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `flowjob_app`
--

DROP TABLE IF EXISTS `flowjob_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_app` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `created_at` datetime(6) DEFAULT NULL,
                               `updated_at` datetime(6) DEFAULT NULL,
                               `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                               `tenant_id` bigint DEFAULT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_app`
--

LOCK TABLES `flowjob_app` WRITE;
/*!40000 ALTER TABLE `flowjob_app` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_broker`
--

DROP TABLE IF EXISTS `flowjob_broker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_broker` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                  `host` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                  `port` int DEFAULT NULL,
                                  `last_heartbeat` datetime(6) DEFAULT NULL,
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `updated_at` datetime(6) DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `idx_last_heartbeat` (`last_heartbeat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_broker`
--

LOCK TABLES `flowjob_broker` WRITE;
/*!40000 ALTER TABLE `flowjob_broker` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_broker` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_job_instance`
--

DROP TABLE IF EXISTS `flowjob_job_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_job_instance` (
                                        `id` bigint NOT NULL AUTO_INCREMENT,
                                        `created_at` datetime(6) DEFAULT NULL,
                                        `updated_at` datetime(6) DEFAULT NULL,
                                        `attributes` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                        `end_at` datetime(6) DEFAULT NULL,
                                        `job_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                        `plan_id` bigint DEFAULT NULL,
                                        `plan_info_id` bigint DEFAULT NULL,
                                        `plan_instance_id` bigint DEFAULT NULL,
                                        `start_at` datetime(6) DEFAULT NULL,
                                        `status` tinyint DEFAULT NULL,
                                        `trigger_at` datetime(6) DEFAULT NULL,
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_job_instance`
--

LOCK TABLES `flowjob_job_instance` WRITE;
/*!40000 ALTER TABLE `flowjob_job_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_job_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_plan`
--

DROP TABLE IF EXISTS `flowjob_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_plan` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `app_id` bigint DEFAULT NULL,
                                `current_version` bigint DEFAULT NULL,
                                `recently_version` bigint DEFAULT NULL,
                                `is_enabled` bit(1) DEFAULT NULL,
                                `last_feedback_at` datetime(6) DEFAULT NULL,
                                `next_trigger_at` datetime(6) DEFAULT NULL,
                                `created_at` datetime(6) DEFAULT NULL,
                                `updated_at` datetime(6) DEFAULT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_plan`
--

LOCK TABLES `flowjob_plan` WRITE;
/*!40000 ALTER TABLE `flowjob_plan` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_plan_info`
--

DROP TABLE IF EXISTS `flowjob_plan_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_plan_info` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `created_at` datetime(6) DEFAULT NULL,
                                     `updated_at` datetime(6) DEFAULT NULL,
                                     `description` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                     `is_deleted` bit(1) DEFAULT NULL,
                                     `jobs` text COLLATE utf8mb4_bin,
                                     `plan_id` bigint DEFAULT NULL,
                                     `retry` int DEFAULT NULL,
                                     `schedule_cron` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                     `schedule_cron_type` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                     `schedule_delay` bigint DEFAULT NULL,
                                     `schedule_interval` bigint DEFAULT NULL,
                                     `schedule_start_at` datetime(6) DEFAULT NULL,
                                     `schedule_type` tinyint DEFAULT NULL,
                                     `trigger_type` tinyint DEFAULT NULL,
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_plan_info`
--

LOCK TABLES `flowjob_plan_info` WRITE;
/*!40000 ALTER TABLE `flowjob_plan_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_plan_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_plan_instance`
--

DROP TABLE IF EXISTS `flowjob_plan_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_plan_instance` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `created_at` datetime(6) DEFAULT NULL,
                                         `updated_at` datetime(6) DEFAULT NULL,
                                         `expect_trigger_at` datetime(6) DEFAULT NULL,
                                         `feedback_at` datetime(6) DEFAULT NULL,
                                         `plan_id` bigint DEFAULT NULL,
                                         `plan_info_id` bigint DEFAULT NULL,
                                         `status` tinyint DEFAULT NULL,
                                         `trigger_at` datetime(6) DEFAULT NULL,
                                         `trigger_type` tinyint DEFAULT NULL,
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_plan_instance`
--

LOCK TABLES `flowjob_plan_instance` WRITE;
/*!40000 ALTER TABLE `flowjob_plan_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_plan_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_plan_slot`
--

DROP TABLE IF EXISTS `flowjob_plan_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_plan_slot` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `created_at` datetime(6) DEFAULT NULL,
                                     `updated_at` datetime(6) DEFAULT NULL,
                                     `plan_id` bigint DEFAULT NULL,
                                     `slot` int DEFAULT NULL,
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_plan_slot`
--

LOCK TABLES `flowjob_plan_slot` WRITE;
/*!40000 ALTER TABLE `flowjob_plan_slot` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_plan_slot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_task`
--

DROP TABLE IF EXISTS `flowjob_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_task` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `created_at` datetime(6) DEFAULT NULL,
                                `updated_at` datetime(6) DEFAULT NULL,
                                `attributes` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                `end_at` datetime(6) DEFAULT NULL,
                                `error_msg` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                `error_stack_trace` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                `job_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                `job_instance_id` bigint DEFAULT NULL,
                                `plan_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                `result` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                `start_at` datetime(6) DEFAULT NULL,
                                `status` tinyint DEFAULT NULL,
                                `worker_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_task`
--

LOCK TABLES `flowjob_task` WRITE;
/*!40000 ALTER TABLE `flowjob_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_tenant`
--

DROP TABLE IF EXISTS `flowjob_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_tenant` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `updated_at` datetime(6) DEFAULT NULL,
                                  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_tenant`
--

LOCK TABLES `flowjob_tenant` WRITE;
/*!40000 ALTER TABLE `flowjob_tenant` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_tenant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_worker`
--

DROP TABLE IF EXISTS `flowjob_worker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_worker` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `app_id` bigint NOT NULL,
                                  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                  `host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                  `port` int DEFAULT NULL,
                                  `protocol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                  `status` tinyint DEFAULT NULL,
                                  `deleted` bit(1) DEFAULT NULL,
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `updated_at` datetime(6) DEFAULT NULL,
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_worker`
--

LOCK TABLES `flowjob_worker` WRITE;
/*!40000 ALTER TABLE `flowjob_worker` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_worker` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_worker_executor`
--

DROP TABLE IF EXISTS `flowjob_worker_executor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_worker_executor` (
                                           `id` bigint NOT NULL AUTO_INCREMENT,
                                           `worker_id` bigint NOT NULL,
                                           `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                           `description` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                           `created_at` datetime(6) DEFAULT NULL,
                                           `updated_at` datetime(6) DEFAULT NULL,
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_worker_executor`
--

LOCK TABLES `flowjob_worker_executor` WRITE;
/*!40000 ALTER TABLE `flowjob_worker_executor` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_worker_executor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_worker_metric`
--

DROP TABLE IF EXISTS `flowjob_worker_metric`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_worker_metric` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `worker_id` bigint NOT NULL,
                                         `available_cpu` float DEFAULT NULL,
                                         `available_queue_limit` int DEFAULT NULL,
                                         `available_ram` float DEFAULT NULL,
                                         `executing_jobs` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
                                         `created_at` datetime(6) DEFAULT NULL,
                                         `updated_at` datetime(6) DEFAULT NULL,
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_worker_metric`
--

LOCK TABLES `flowjob_worker_metric` WRITE;
/*!40000 ALTER TABLE `flowjob_worker_metric` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_worker_metric` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flowjob_worker_tag`
--

DROP TABLE IF EXISTS `flowjob_worker_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_worker_tag` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `worker_id` bigint NOT NULL,
                                         `tag_key` varchar(255) COLLATE utf8mb4_bin DEFAULT '' NOT NULL,
                                         `tag_value` varchar(255) COLLATE utf8mb4_bin DEFAULT '' NOT NULL,
                                         `created_at` datetime(6) DEFAULT NULL,
                                         `updated_at` datetime(6) DEFAULT NULL,
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_worker_tag`
--

LOCK TABLES `flowjob_worker_tag` WRITE;
/*!40000 ALTER TABLE `flowjob_worker_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_worker_tag` ENABLE KEYS */;
UNLOCK TABLES;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-10-08 10:16:46
