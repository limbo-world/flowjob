ALTER TABLE `worker_metric` ADD COLUMN `available_queue_limit` int NOT NULL COMMENT '队列剩余可用数' AFTER `available_ram`;
