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
DROP TABLE IF EXISTS `flowjob_delay_instance`;
CREATE TABLE `flowjob_delay_instance`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT,
    `instance_id`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `biz_type`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `biz_id`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `instance_type` tinyint                                                NOT NULL,
    `status`        tinyint                                                NOT NULL,
    `job_info`      text COLLATE utf8mb4_bin,
    `attributes`    text COLLATE utf8mb4_bin,
    `trigger_at`    datetime(6) NOT NULL,
    `start_at`      datetime(6) DEFAULT NULL,
    `feedback_at`   datetime(6) DEFAULT NULL,
    `is_deleted`    bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`    datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`instance_id`),
    UNIQUE KEY `uk_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

update `flowjob_id`
set `type` = "INSTANCE"
where `type` = "PLAN_INSTANCE";

DROP TABLE IF EXISTS `flowjob_job_instance`;
CREATE TABLE `flowjob_job_instance`
(
    `id`              bigint unsigned NOT NULL AUTO_INCREMENT,
    `job_instance_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `instance_id`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `instance_type`   tinyint                                                NOT NULL,
    `job_id`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          DEFAULT NULL,
    `agent_id`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `retry_times`     int unsigned NOT NULL DEFAULT 1,
    `plan_id`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `plan_info_id`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `broker_url`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `trigger_at`      datetime(6) NOT NULL,
    `context`         text COLLATE utf8mb4_bin,
    `start_at`        datetime(6) DEFAULT NULL,
    `end_at`          datetime(6) DEFAULT NULL,
    `last_report_at`  datetime(6) NOT NULL,
    `status`          tinyint                                                NOT NULL,
    `error_msg`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `is_deleted`      bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at`      datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`job_instance_id`),
    UNIQUE KEY `uk_instance_job` (`instance_id`, `job_id`, `retry_times`),
    KEY               `idx_report_broker` (`last_report_at`, `broker_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

DROP TABLE IF EXISTS `flowjob_lock`;
CREATE TABLE `flowjob_lock`
(
    `id`         bigint unsigned NOT NULL AUTO_INCREMENT,
    `name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `owner`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
    `expire_at`  datetime(6) NOT NULL,
    `is_deleted` bit(1)                                                 NOT NULL DEFAULT 0,
    `created_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
