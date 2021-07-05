package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Brozen
 * @since 2021-07-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("worker_executor")
public class WorkerExecutorPO extends PO {

    private static final long serialVersionUID = 7370406980674258946L;

    /**
     * worker节点ID
     */
    private String workerId;

    /**
     * 执行器名称
     */
    private String executorName;

    /**
     * 执行器描述信息
     */
    private String executorDesc;

    /**
     * 执行器类型
     */
    private Byte executeType;

}
