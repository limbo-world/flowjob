create table job
(
    serial_id bigint auto_increment comment '自增ID' primary key,
    job_id varchar(64) not null comment '作业唯一标识ID',
    job_desc varchar(128) default '' not null comment '作业描述',
    dispatch_type tinyint not null comment '作业分发方式',
    cpu_requirement double default 0 not null comment '所需的CPU核心数',
    ram_requirement double default 0 not null comment '所需的内存GB数',
    schedule_type tinyint not null comment '作业调度方式',
    schedule_delay bigint default 0 not null comment '作业延迟时间，单位毫秒',
    schedule_interval bigint default 0 not null comment '作业调度间隔时间，单位毫秒',
    schedule_cron varchar(64) default '' not null comment '作业调度的CRON表达式',
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_job unique (job_id)
)
comment '作业';

create table job_execute_record
(
    serial_id bigint auto_increment primary key,
    job_id varchar(64) default '' not null comment '作业ID',
    record_id varchar(64) default '' not null comment '执行记录ID',
    status tinyint not null comment '执行状态',
    worker_id varchar(64) default '' not null comment '执行作业的worker',
    attributes text null comment '此次执行的参数',
    created_at datetime default CURRENT_TIMESTAMP null,
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_job_record unique (job_id, record_id)
);

create table worker
(
    serial_id bigint auto_increment primary key,
    worker_id varchar(64) default '' not null comment 'worker节点ID，根据ip、host、protocol计算得到',
    protocol tinyint not null comment 'worker服务使用的通信协议',
    ip varchar(64) not null comment 'worker服务的通信IP',
    port int not null comment 'worker服务的通信端口',
    status tinyint not null comment 'worker节点状态',
    deleted bit default b'0' not null comment '是否已删除',
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
comment '工作节点';

create table worker_executor
(
    worker_id varchar(64) not null comment 'worker id',
    executor_name varchar(64) not null comment '执行器名称',
    executor_desc varchar(256) default '' not null comment '执行器描述',
    execute_type tinyint default 0 not null comment '执行方式',
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    primary key (worker_id, executor_name)
)
comment 'worker执行器';

create table worker_metric
(
    worker_id varchar(64) not null comment 'worker节点ID' primary key,
    executing_jobs text null comment 'worker节点上正在执行中的作业',
    available_cpu double default 0 not null comment '可用的CPU核心数',
    available_ram double default 0 not null comment '可用的内存空间，单位GB',
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
);

create table worker_statistics
(
    worker_id varchar(64) not null comment 'worker节点ID' primary key,
    job_dispatch_count bigint default 0 not null comment '作业下发到此worker的次数',
    latest_dispatch_time datetime default '2000-01-00 00:00:00' not null comment '最后一次向此worker下发作业成功的时间',
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
);

