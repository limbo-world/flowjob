package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.common.constants.JobType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * job的一次执行 对应于PlanRecord
 *
 * @author Devil
 * @since 2021/9/1
 */
@Setter
@Getter
@Table(name = "flowjob_job_info")
@Entity
@DynamicInsert
@DynamicUpdate
public class JobInfoEntity extends BaseEntity {
    private static final long serialVersionUID = -1136312243146520057L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 对应的planInfo
     */
    private String planInfoId;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 类型
     * @see JobType
     */
    private Byte type;

    /**
     * 触发类型
     * @see TriggerType
     */
    private Byte triggerType;

    /**
     * 属性参数
     * @see Attributes
     */
    protected String attributes;

    /**
     * 作业分发配置参数
     * @see DispatchOption
     */
    private String dispatchOption;

    /**
     * 作业执行器配置参数
     */
    private String executorName;

    /**
     * 执行失败是否终止 false 会继续执行后续作业
     */
    private Boolean terminateWithFail;

    @Override
    public Object getUid() {
        return id;
    }

}
