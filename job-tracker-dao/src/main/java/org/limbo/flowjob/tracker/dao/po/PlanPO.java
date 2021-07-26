package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * plan
 *
 * @author Devil
 * @since 2021/7/23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_plan")
public class PlanPO extends PO {

    private static final long serialVersionUID = -6323915044280199312L;

    /**
     * DB自增序列ID，并不是唯一标识
     */
    private Long serialId;

    /**
     * 作业执行计划ID
     */
    @TableId(type = IdType.INPUT)
    private String planId;

    /**
     * 当前版本
     */
    private Integer currentVersion;

    /**
     * 最新版本
     */
    private Integer recentlyVersion;

    /**
     * 是否启动 新建plan的时候 默认为不启动
     * 接口调用的时候会修改 leader 内存数据以及 db数据 需要保障一致性
     */
    private Boolean isEnabled;

    /**
     * 是否删除
     */
    private Boolean isDeleted;
}
