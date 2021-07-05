package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("worker")
public class WorkerPO extends PO {

    private static final long serialVersionUID = -3237766932023820195L;

    private Long serialId;

    /**
     * worker节点ID，根据ip、host、protocol计算得到
     */
    @TableId(type = IdType.INPUT)
    private String workerId;

    /**
     * worker服务使用的通信协议
     */
    private Byte protocol;

    /**
     * worker服务的通信IP
     */
    private String ip;

    /**
     * worker服务的通信端口
     */
    private Integer port;

    /**
     * worker节点状态
     */
    private Byte status;

    /**
     * 节点是否被删除
     */
    private Boolean deleted;

}
