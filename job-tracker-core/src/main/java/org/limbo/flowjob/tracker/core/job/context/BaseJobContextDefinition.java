package org.limbo.flowjob.tracker.core.job.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.limbo.flowjob.tracker.core.job.attribute.ImmutableJobAttribute;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class BaseJobContextDefinition implements JobContextDefinition {

    /**
     * 作业ID
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String jobId;

    /**
     * 执行上下文ID
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String contextId;

    /**
     * 此上下文状态
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private JobContext.Status status;

    /**
     * 此分发执行此作业上下文的worker
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String workerId;

    /**
     * 此上下文的创建时间
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private LocalDateTime createdAt;

    /**
     * 此上下文的更新时间
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private LocalDateTime updatedAt;

    /**
     * 作业属性，不可变。
     */
    @Getter
    private ImmutableJobAttribute jobAttributes;

    public BaseJobContextDefinition(String jobId, String contextId, JobContext.Status status, String workerId) {
        this(jobId, contextId, status, workerId, Collections.emptyMap());
    }

    public BaseJobContextDefinition(String jobId, String contextId, JobContext.Status status, String workerId,
                            Map<String, List<String>> attributes) {
        this.jobId = jobId;
        this.contextId = contextId;
        this.status = status;
        this.workerId = workerId;

        this.jobAttributes = new ImmutableJobAttribute(attributes);
    }

}
