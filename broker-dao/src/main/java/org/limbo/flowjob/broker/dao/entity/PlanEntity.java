package org.limbo.flowjob.broker.dao.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * plan 计划
 *
 * @author Devil
 * @since 2021/7/23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "flowjob_plan")
@Entity
@DynamicInsert
@DynamicUpdate
public class PlanEntity extends BaseEntity {

    private static final long serialVersionUID = -6323915044280199312L;

    /**
     * 当前版本。可能发生回滚，因此 currentVersion 可能小于 recentlyVersion 。
     * 对应 planInfoId
     */
    private String currentVersion;

    /**
     * 最新版本
     * 对应 planInfoId
     */
    private String recentlyVersion;

    /**
     * 是否启动 新建plan的时候 默认为不启动
     * 接口调用的时候会修改 leader 内存数据以及 db数据 需要保障一致性
     */
    private Boolean isEnabled;

}
