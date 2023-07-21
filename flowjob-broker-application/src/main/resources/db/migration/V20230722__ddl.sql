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
