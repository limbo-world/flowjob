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

--
-- Table structure for table `flowjob_app`
--

DROP TABLE IF EXISTS `flowjob_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_app`
(
    `id`         bigint unsigned NOT NULL AUTO_INCREMENT,
    `app_id`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `tenant_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `is_deleted` bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_app`
--

LOCK
TABLES `flowjob_app` WRITE;
/*!40000 ALTER TABLE `flowjob_app` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_app` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_broker`
--

DROP TABLE IF EXISTS `flowjob_broker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_broker`
(
    `id`             bigint unsigned NOT NULL AUTO_INCREMENT,
    `broker_id`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `name`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `host`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `port`           int                                                             DEFAULT NULL,
    `online_time`    datetime(6) DEFAULT NULL,
    `last_heartbeat` datetime(6) DEFAULT NULL,
    `is_deleted`     bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`     datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`id`),
    KEY              `idx_name` (`name`),
    KEY              `idx_online_time` (`online_time`),
    KEY              `idx_last_heartbeat` (`last_heartbeat`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_broker`
--

LOCK
TABLES `flowjob_broker` WRITE;
/*!40000 ALTER TABLE `flowjob_broker` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_broker` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_plan`
--

DROP TABLE IF EXISTS `flowjob_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_plan`
(
    `id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `plan_id`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `app_id`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `current_version`  int(8) unsigned NOT NULL,
    `recently_version` int(8) unsigned NOT NULL,
    `name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `is_enabled`       bit(1)                                                          DEFAULT NULL,
    `is_deleted`       bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`       datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`plan_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_plan`
--

LOCK
TABLES `flowjob_plan` WRITE;
/*!40000 ALTER TABLE `flowjob_plan` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_plan` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_plan_info`
--

DROP TABLE IF EXISTS `flowjob_plan_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_plan_info`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `plan_info_id`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `plan_id`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `plan_type`          tinyint                                                NOT NULL,
    `trigger_type`       tinyint                                                NOT NULL,
    `schedule_type`      tinyint                                                NOT NULL,
    `schedule_cron`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `schedule_cron_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `schedule_delay`     bigint                                                          DEFAULT NULL,
    `schedule_interval`  bigint                                                          DEFAULT NULL,
    `schedule_start_at`  datetime(6) DEFAULT NULL,
    `schedule_end_at`    datetime(6) DEFAULT NULL,
    `job_info`           text COLLATE utf8mb4_bin,
    `name`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `description`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `is_deleted`         bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`         datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`plan_info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_plan_info`
--

LOCK
TABLES `flowjob_plan_info` WRITE;
/*!40000 ALTER TABLE `flowjob_plan_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_plan_info` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_plan_instance`
--

DROP TABLE IF EXISTS `flowjob_plan_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_plan_instance`
(
    `id`               bigint unsigned NOT NULL AUTO_INCREMENT,
    `plan_instance_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `plan_id`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `plan_info_id`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `trigger_type`     tinyint                                                NOT NULL,
    `schedule_type`    tinyint                                                NOT NULL,
    `status`           tinyint                                                NOT NULL,
    `trigger_at`       datetime(6) NOT NULL,
    `start_at`         datetime(6) DEFAULT NULL,
    `feedback_at`      datetime(6) DEFAULT NULL,
    `is_deleted`       bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`       datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`plan_instance_id`),
    KEY                `idx_plan_trigger` (`plan_id`, `trigger_at`),
    KEY                `idx_plan_feedback` (`plan_id`, `feedback_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_plan_instance`
--

LOCK
TABLES `flowjob_plan_instance` WRITE;
/*!40000 ALTER TABLE `flowjob_plan_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_plan_instance` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_job_instance`
--

DROP TABLE IF EXISTS `flowjob_job_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_job_instance`
(
    `id`               bigint unsigned NOT NULL AUTO_INCREMENT,
    `job_instance_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `job_id`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `plan_instance_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `agent_id`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `retry_times`      int unsigned NOT NULL DEFAULT 1,
    `plan_id`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `plan_info_id`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `trigger_at`       datetime(6) NOT NULL,
    `context`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `start_at`         datetime(6) DEFAULT NULL,
    `end_at`           datetime(6) DEFAULT NULL,
    `status`           tinyint                                                NOT NULL,
    `error_msg`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `is_deleted`       bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`       datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`job_instance_id`),
    UNIQUE KEY `uk_plan_instance_job` (`plan_instance_id`, `job_id`, `retry_times`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_job_instance`
--

LOCK
TABLES `flowjob_job_instance` WRITE;
/*!40000 ALTER TABLE `flowjob_job_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_job_instance` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_tenant`
--

DROP TABLE IF EXISTS `flowjob_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_tenant`
(
    `id`         bigint unsigned NOT NULL AUTO_INCREMENT,
    `tenant_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `is_deleted` bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_tenant`
--

LOCK
TABLES `flowjob_tenant` WRITE;
/*!40000 ALTER TABLE `flowjob_tenant` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_tenant` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_worker`
--

DROP TABLE IF EXISTS `flowjob_worker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_worker`
(
    `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `worker_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `app_id`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `host`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `port`       int                                                             DEFAULT NULL,
    `protocol`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `status`     tinyint                                                NOT NULL,
    `is_enabled` bit(1)                                                          DEFAULT NULL,
    `is_deleted` bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`worker_id`),
    KEY          `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_worker`
--

LOCK
TABLES `flowjob_worker` WRITE;
/*!40000 ALTER TABLE `flowjob_worker` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_worker` ENABLE KEYS */;
UNLOCK
TABLES;

DROP TABLE IF EXISTS `flowjob_worker_slot`;
CREATE TABLE `flowjob_worker_slot`
(
    `id`         bigint unsigned NOT NULL AUTO_INCREMENT,
    `worker_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `slot`       int                                                    NOT NULL,
    `is_deleted` bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`worker_id`),
    KEY          `idx_slot` (`slot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


--
-- Table structure for table `flowjob_worker_executor`
--

DROP TABLE IF EXISTS `flowjob_worker_executor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_worker_executor`
(
    `id`                 bigint unsigned NOT NULL AUTO_INCREMENT,
    `worker_executor_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `worker_id`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `name`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `description`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `is_deleted`         bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`         datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`worker_executor_id`),
    KEY                  `idx_worker` (`worker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_worker_executor`
--

LOCK
TABLES `flowjob_worker_executor` WRITE;
/*!40000 ALTER TABLE `flowjob_worker_executor` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_worker_executor` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_worker_metric`
--

DROP TABLE IF EXISTS `flowjob_worker_metric`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_worker_metric`
(
    `id`                    bigint unsigned NOT NULL AUTO_INCREMENT,
    `worker_id`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `available_cpu`         float                                                           DEFAULT NULL,
    `available_queue_limit` int                                                             DEFAULT NULL,
    `available_ram`         bigint                                                          DEFAULT NULL,
    `last_heartbeat_at`     datetime(6) NOT NULL,
    `is_deleted`            bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`            datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`worker_id`),
    KEY                     `idx_last_heartbeat` (`last_heartbeat_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_worker_metric`
--

LOCK
TABLES `flowjob_worker_metric` WRITE;
/*!40000 ALTER TABLE `flowjob_worker_metric` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_worker_metric` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_worker_tag`
--

DROP TABLE IF EXISTS `flowjob_worker_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_worker_tag`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT,
    `worker_tag_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `worker_id`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `tag_key`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `tag_value`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `is_deleted`    bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`    datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`worker_tag_id`),
    KEY             `idx_worker` (`worker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_worker_tag`
--

LOCK
TABLES `flowjob_worker_tag` WRITE;
/*!40000 ALTER TABLE `flowjob_worker_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_worker_tag` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_plan_slot`
--

DROP TABLE IF EXISTS `flowjob_plan_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_plan_slot`
(
    `id`         bigint unsigned NOT NULL AUTO_INCREMENT,
    `plan_id`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `slot`       int                                                    NOT NULL,
    `is_deleted` bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`plan_id`),
    KEY          `idx_slot` (`slot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_plan_slot`
--

LOCK
TABLES `flowjob_plan_slot` WRITE;
/*!40000 ALTER TABLE `flowjob_plan_slot` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_plan_slot` ENABLE KEYS */;
UNLOCK
TABLES;

--
-- Table structure for table `flowjob_worker`
--

DROP TABLE IF EXISTS `flowjob_agent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_agent`
(
    `id`                    bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `agent_id`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `protocol`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `host`                  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `port`                  int                                                             DEFAULT NULL,
    `status`                tinyint                                                NOT NULL,
    `available_queue_limit` int                                                             DEFAULT NULL,
    `last_heartbeat_at`     datetime(6) NOT NULL,
    `is_enabled`            bit(1)                                                          DEFAULT NULL,
    `is_deleted`            bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`            datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_agent`
--

LOCK
TABLES `flowjob_agent` WRITE;
/*!40000 ALTER TABLE `flowjob_agent` DISABLE KEYS */;
/*!40000 ALTER TABLE `flowjob_agent` ENABLE KEYS */;
UNLOCK
TABLES;

DROP TABLE IF EXISTS `flowjob_agent_slot`;
CREATE TABLE `flowjob_agent_slot`
(
    `id`         bigint unsigned NOT NULL AUTO_INCREMENT,
    `agent_id`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `slot`       int                                                    NOT NULL,
    `is_deleted` bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`agent_id`),
    KEY          `idx_slot` (`slot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

--
-- Table structure for table `flowjob_id`
--

DROP TABLE IF EXISTS `flowjob_id`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flowjob_id`
(
    `id`         bigint unsigned NOT NULL AUTO_INCREMENT,
    `type`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `current_id` bigint unsigned NOT NULL DEFAULT '0',
    `step`       int                                                    NOT NULL DEFAULT 0,
    `is_deleted` bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flowjob_id`
--

LOCK
TABLES `flowjob_id` WRITE;
/*!40000 ALTER TABLE `flowjob_id` DISABLE KEYS */;
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('APP', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('TENANT', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('WORKER', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('WORKER_EXECUTOR', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('WORKER_TAG', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('BROKER', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('PLAN', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('PLAN_INFO', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('PLAN_INSTANCE', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('JOB_INSTANCE', 100000, 1000);
INSERT INTO flowjob_id(`type`, `current_id`, `step`)
VALUES ('AGENT', 100000, 1000);
/*!40000 ALTER TABLE `flowjob_id` ENABLE KEYS */;
UNLOCK
TABLES;
