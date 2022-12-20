package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * plan 计划
 *
 * @author Devil
 * @since 2021/7/23
 */
@Setter
@Getter
@Table(name = "flowjob_plan")
@Entity
@DynamicInsert
@DynamicUpdate
public class PlanEntity extends BaseEntity {
    private static final long serialVersionUID = -6323915044280199312L;

    /**
     * 数据库自增id
     */
    @Column(updatable = false)
    private Long id;

    @Id
    private String planId;
    /**
     * 所属应用
     */
    private String appId;
    /**
     * 下次调度触发时间
     */
    private LocalDateTime nextTriggerAt;
    /**
     * 上次调度时间
     */
    private LocalDateTime lastScheduleAt;
    /**
     * 上次完成时间
     */
    private LocalDateTime lastFeedbackAt;
    /**
     * 当前版本。可能发生回滚，因此 currentVersion 可能小于 recentlyVersion 。
     */
    private Integer currentVersion;

    /**
     * 最新版本
     */
    private Integer recentlyVersion;

    /**
     * 是否启动 新建plan的时候 默认为不启动
     */
    @Column(name = "is_enabled")
    private boolean enabled;

    @Override
    public Object getUid() {
        return planId;
    }
}
