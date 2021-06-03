package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Data
public class WorkerMetricPO implements Serializable {

    private static final long serialVersionUID = -3009642474389520555L;

    /**
     * worker节点ID
     */
    @TableId(type = IdType.INPUT)
    private String workerId;

    /**
     * worker节点上正在执行中的作业
     */
    private String executingJobs;

    /**
     * 可用的CPU核心数
     */
    private Float availableCpu;

    /**
     * 可用的内存空间，单位GB
     */
    private Float availableRam;

    private LocalDateTime updatedAt;

}
