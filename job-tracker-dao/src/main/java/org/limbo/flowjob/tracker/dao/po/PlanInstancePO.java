package org.limbo.flowjob.tracker.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计划实例
 *
 * @author Devil
 * @date 2021/7/15 9:54 上午
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_instance")
public class PlanInstancePO extends PO {

    private static final long serialVersionUID = -8354897444427352804L;
    /**
     * DB自增序列ID 唯一
     */
    private Long serialId;

    /**
     * 计划ID
     */
    private String planId;

}
