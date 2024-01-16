package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
     * 分配的节点 ip:host
     */
    private String brokerUrl;

    /**
     * 所属应用
     */
    private String appId;

    /**
     * 当前版本。可能发生回滚，因此 currentVersion 可能小于 recentlyVersion 。
     */
    private String currentVersion;

    /**
     * 最新版本
     */
    private String recentlyVersion;

    /**
     * 是否启动 新建plan的时候 默认为不启动
     */
    @Column(name = "is_enabled")
    private boolean enabled;

    /**
     * 冗余字段，用于查询
     */
    private String name;

    @Override
    public Object getUid() {
        return planId;
    }
}
