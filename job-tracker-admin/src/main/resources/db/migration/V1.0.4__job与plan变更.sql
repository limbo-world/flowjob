CREATE TABLE plan
(
    serial_id BIGINT AUTO_INCREMENT COMMENT '自增ID' PRIMARY KEY,
    plan_id VARCHAR(64) NOT NULL COMMENT '作业执行计划ID',
    plan_desc VARCHAR(128) DEFAULT '' NOT NULL COMMENT '执行计划描述',
    dispatch_type TINYINT NOT NULL COMMENT '作业分发方式',
    schedule_type TINYINT NOT NULL COMMENT '作业调度方式',
    schedule_start_at DATETIME NOT NULL DEFAULT '2000-01-01 00:00:00' COMMENT '从何时开始调度作业',
    schedule_delay BIGINT DEFAULT 0 NOT NULL COMMENT '作业延迟时间，单位毫秒',
    schedule_interval BIGINT DEFAULT 0 NOT NULL COMMENT '作业调度间隔时间，单位毫秒',
    schedule_cron VARCHAR(64) DEFAULT '' NOT NULL COMMENT '作业调度的CRON表达式',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_plan UNIQUE (plan_id)
)
COMMENT '作业';

ALTER TABLE job ADD COLUMN `plan_id` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '作业执行计划ID' AFTER `serial_id`;
ALTER TABLE job ADD COLUMN `parent_job_ids` text NOT NULL COMMENT '此作业依赖的父作业ID' AFTER `job_desc`;
ALTER TABLE job DROP COLUMN schedule_type;
ALTER TABLE job DROP COLUMN schedule_delay;
ALTER TABLE job DROP COLUMN schedule_interval;
ALTER TABLE job DROP COLUMN schedule_cron;