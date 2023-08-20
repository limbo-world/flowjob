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

package org.limbo.flowjob.agent.core.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.core.FlowjobConnectionFactory;
import org.limbo.flowjob.agent.core.Task;
import org.limbo.flowjob.agent.core.worker.Worker;
import org.limbo.flowjob.api.constants.TaskStatus;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.api.param.console.TaskQueryParam;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.time.DateTimeUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Devil
 * @since 2023/8/3
 */
@Slf4j
public class TaskRepository {

    private FlowjobConnectionFactory connectionFactory;

    private static final String TABLE_NAME = "flowjob_task";

    public TaskRepository(FlowjobConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public boolean existTable() throws SQLException {
        try (Connection conn = connectionFactory.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, TABLE_NAME, null);
            return tables.next();
        }
    }

    public void initTable() throws SQLException {
        try (Connection conn = connectionFactory.getConnection(); Statement stat = conn.createStatement()) {
            String dropSql = "DROP TABLE IF EXISTS `" + TABLE_NAME + "`;";
            stat.execute(dropSql);

            String createSql = "CREATE TABLE `" + TABLE_NAME + "`\n" +
                    "(\n" +
                    "    `id`                bigint unsigned NOT NULL AUTO_INCREMENT,\n" +
                    "    `task_id`           varchar(255) NOT NULL DEFAULT '',\n" +
                    "    `job_id`            varchar(255) NOT NULL DEFAULT '',\n" +
                    "    `worker_id`         varchar(255) NOT NULL DEFAULT '',\n" +
                    "    `worker_address`    varchar(255) NOT NULL DEFAULT '',\n" +
                    "    `executor_name`     varchar(255) NOT NULL DEFAULT '',\n" +
                    "    `context`           text,\n" +
                    "    `job_attributes`    text,\n" +
                    "    `task_attributes`   text,\n" +
                    "    `type`              int                                                    NOT NULL,\n" +
                    "    `status`            int                                                    NOT NULL,\n" +
                    "    `trigger_at`        datetime(6) DEFAULT NULL,\n" +
                    "    `start_at`          datetime(6) DEFAULT NULL,\n" +
                    "    `end_at`            datetime(6) DEFAULT NULL,\n" +
                    "    `result`            varchar(255) DEFAULT '',\n" +
                    "    `error_msg`         varchar(255) DEFAULT '',\n" +
                    "    `last_report_at`    datetime(6) NOT NULL," +
                    "    `error_stack_trace` text DEFAULT NULL,\n" +
                    "    `created_at`        datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    `updated_at`        datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                    "    PRIMARY KEY (`id`),\n" +
                    "    UNIQUE INDEX       `idx_job_task` (`job_id`, `task_id`),\n" +
                    "    INDEX              `idx_report_task` (`last_report_at`, `task_id`)\n" +
                    ")";
            stat.execute(createSql);
        }
    }

    public Task getById(String jobId, String taskId) {
        String sql = "select * from " + TABLE_NAME + " where job_id = ? and task_id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jobId);
            ps.setString(2, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return convert(rs);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("TaskRepository.getById error jobId={} taskId={}", jobId, taskId, e);
            return null;
        }
    }

    public List<Task> getByLastReportBeforeAndUnFinish(String lastTime, String startId, Integer limit) {
        String sql = "select * from " + TABLE_NAME + " where last_report_at < ? and status not in (?, ?) and task_id > ? order by task_id limit ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lastTime);
            ps.setInt(2, TaskStatus.SUCCEED.status);
            ps.setInt(3, TaskStatus.FAILED.status);
            ps.setString(4, startId);
            ps.setInt(5, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> tasks = new ArrayList<>();
                while (rs.next()) {
                    tasks.add(convert(rs));
                }
                return tasks;
            }
        } catch (Exception e) {
            log.error("TaskRepository.getByLastReportAtBefore error lastTime={} startId={} limit={}", lastTime, startId, limit, e);
            return Collections.emptyList();
        }
    }

    public List<Task> getByJobAndType(String jobId, TaskType type, String startId, Integer limit) {
        String sql = "select * from " + TABLE_NAME + " where job_id = ? and `type` = ? and task_id > ? order by task_id limit ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jobId);
            ps.setInt(2, type.type);
            ps.setString(3, startId);
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> tasks = new ArrayList<>();
                while (rs.next()) {
                    tasks.add(convert(rs));
                }
                return tasks;
            }
        } catch (Exception e) {
            log.error("TaskRepository.getByJobAndType error jobId={} type={} startId={} limit={}", jobId, type, startId, limit, e);
            return Collections.emptyList();
        }
    }

    public List<Task> queryPage(TaskQueryParam param) {
        String sql = "select * from " + TABLE_NAME + " where job_id = ? LIMIT ? OFFSET ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param.getJobInstanceId());
            ps.setInt(2, param.getSize());
            ps.setInt(3, param.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> tasks = new ArrayList<>();
                while (rs.next()) {
                    tasks.add(convert(rs));
                }
                return tasks;
            }
        } catch (Exception e) {
            log.error("TaskRepository.queryPage error param={}", param, e);
            return Collections.emptyList();
        }
    }

    public long queryCount(TaskQueryParam param) {
        String sql = "select count(*) from " + TABLE_NAME + " where job_id = ? LIMIT ? OFFSET ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param.getJobInstanceId());
            ps.setInt(2, param.getSize());
            ps.setInt(3, param.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0L;
                }
            }
        } catch (Exception e) {
            log.error("TaskRepository.queryCount error param={}", param, e);
            return 0L;
        }
    }

    public List<String> getAllTaskResult(String jobId, TaskType type) {
        String sql = "select result from " + TABLE_NAME + " where job_id = ? and `type` = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jobId);
            ps.setInt(2, type.type);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rs.getString("result"));
                }
                return results;
            }
        } catch (Exception e) {
            log.error("TaskRepository.getAllTaskResult error jobId={} type={}", jobId, type, e);
            return Collections.emptyList();
        }
    }

    public long countUnSuccess(String jobId, TaskType type) {
        String sql = "select count(*) from " + TABLE_NAME + " where job_id = ? and `type` = ? and `status` != ? ";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jobId);
            ps.setInt(2, type.type);
            ps.setInt(3, TaskStatus.SUCCEED.status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0L;
                }
            }
        } catch (Exception e) {
            log.error("TaskRepository.countUnSuccess error jobId={} type={}", jobId, type, e);
            return 0L;
        }
    }

    public boolean batchSave(Collection<Task> tasks) {
        List<String> values = new ArrayList<>();
        for (Task task : tasks) {
            values.add(" (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
        String sql = "insert into " + TABLE_NAME + "(" +
                "task_id, job_id, worker_id, worker_address, executor_name, context, job_attributes, task_attributes, `type`, " +
                "status, trigger_at, start_at, end_at, `result`, error_msg, error_stack_trace, last_report_at" +
                ") values " + StringUtils.join(values, ",");

        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 0;
            for (Task task : tasks) {
                ps.setString(++idx, task.getTaskId());
                ps.setString(++idx, task.getJobId());
                if (task.getWorker() != null) {
                    ps.setString(++idx, task.getWorker().getId());
                    ps.setString(++idx, task.getWorker().address());
                } else {
                    ps.setString(++idx, "");
                    ps.setString(++idx, "");
                }
                ps.setString(++idx, task.getExecutorName());
                ps.setString(++idx, task.getContext().toString());
                ps.setString(++idx, task.getJobAttributes().toString());
                ps.setString(++idx, task.getTaskAttributes());
                ps.setInt(++idx, task.getType().type);
                ps.setInt(++idx, task.getStatus().status);
                ps.setString(++idx, task.getTriggerAt() == null ? null : DateTimeUtils.formatYMDHMS(task.getTriggerAt()));
                ps.setString(++idx, task.getStartAt() == null ? null : DateTimeUtils.formatYMDHMS(task.getStartAt()));
                ps.setString(++idx, task.getEndAt() == null ? null : DateTimeUtils.formatYMDHMS(task.getEndAt()));
                ps.setString(++idx, task.getResult() == null ? "" : task.getResult());
                ps.setString(++idx, task.getErrorMsg() == null ? "" : task.getErrorMsg());
                ps.setString(++idx, task.getErrorStackTrace() == null ? "" : task.getErrorStackTrace());
                ps.setString(++idx, DateTimeUtils.formatYMDHMS(TimeUtils.currentLocalDateTime()));
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("TaskRepository.batchSave error", e);
            return false;
        }
    }


    public boolean executing(Task task) {
        String sql = "update " + TABLE_NAME + " set `status` = ?, worker_id = ?, worker_address = ?, start_at = ? where job_id = ? and task_id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, TaskStatus.EXECUTING.status);
            ps.setString(2, task.getWorker().getId());
            ps.setString(3, task.getWorker().address());
            ps.setString(4, DateTimeUtils.formatYMDHMS(TimeUtils.currentLocalDateTime()));
            ps.setString(5, task.getJobId());
            ps.setString(6, task.getTaskId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("TaskRepository.executing error task={} ", task, e);
            return false;
        }
    }

    public boolean report(String jobId, String taskId) {
        String sql = "update " + TABLE_NAME + " set `last_report_at` = ? where job_id = ? and task_id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DateTimeUtils.formatYMDHMS(TimeUtils.currentLocalDateTime()));
            ps.setString(2, jobId);
            ps.setString(3, taskId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("TaskRepository.report error jobId={} taskId={}", jobId, taskId, e);
            return false;
        }
    }

    public boolean success(Task task) {
        String sql = "update " + TABLE_NAME + " set `status` = ?, end_at = ?, `result` = ?, context = ? where job_id = ? and task_id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, TaskStatus.SUCCEED.status);
            ps.setString(2, DateTimeUtils.formatYMDHMS(TimeUtils.currentLocalDateTime()));
            ps.setString(3, task.getResult());
            ps.setString(4, task.getContext().toString());
            ps.setString(5, task.getJobId());
            ps.setString(6, task.getTaskId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("TaskRepository.success error task={} ", task, e);
            return false;
        }
    }

    public boolean fail(Task task) {
        String sql = "update " + TABLE_NAME + " set `status` = ?, end_at = ?, error_msg = ?, error_stack_trace = ? where job_id = ? and task_id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, TaskStatus.FAILED.status);
            ps.setString(2, DateTimeUtils.formatYMDHMS(TimeUtils.currentLocalDateTime()));
            ps.setString(3, task.getErrorMsg());
            ps.setString(4, task.getErrorStackTrace());
            ps.setString(5, task.getJobId());
            ps.setString(6, task.getTaskId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("TaskRepository.fail error task={} ", task, e);
            return false;
        }
    }

    private Task convert(ResultSet rs) throws SQLException, MalformedURLException {
        Task task = new Task();
        task.setTaskId(rs.getString("task_id"));
        task.setJobId(rs.getString("job_id"));
        task.setExecutorName(rs.getString("executor_name"));
        task.setType(TaskType.parse(rs.getInt("type")));
        task.setStatus(TaskStatus.parse(rs.getInt("status")));

        String workerId = rs.getString("worker_id");
        String workerAddress = rs.getString("worker_address");
        Worker worker = null;
        if (StringUtils.isNotBlank(workerId) && StringUtils.isNotBlank(workerAddress)) {
            worker = new Worker(workerId, new URL(workerAddress));
        }
        task.setWorker(worker);

        String triggerAtStr = rs.getString("trigger_at");
        String startAtStr = rs.getString("start_at");
        String endAtStr = rs.getString("end_at");
        task.setTriggerAt(StringUtils.isBlank(triggerAtStr) ? null : DateTimeUtils.parseYMDHMS(triggerAtStr));
        task.setStartAt(StringUtils.isBlank(startAtStr) ? null : DateTimeUtils.parseYMDHMS(startAtStr));
        task.setEndAt(StringUtils.isBlank(endAtStr) ? null : DateTimeUtils.parseYMDHMS(endAtStr));

        task.setContext(new Attributes(rs.getString("context")));
        task.setJobAttributes(new Attributes(rs.getString("job_attributes")));
        task.setTaskAttributes(rs.getString("task_attributes"));
        task.setResult(rs.getString("result"));
        task.setErrorMsg(rs.getString("error_msg"));
        task.setErrorStackTrace(rs.getString("error_stack_trace"));
        task.setLastReportAt(DateTimeUtils.parseYMDHMS(rs.getString("last_report_at")));
        return task;
    }

}
