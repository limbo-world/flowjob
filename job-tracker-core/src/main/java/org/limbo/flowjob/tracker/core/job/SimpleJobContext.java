package org.limbo.flowjob.tracker.core.job;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author Brozen
 * @since 2021-05-21
 */
@Slf4j
public class SimpleJobContext extends JobContextLifecycle implements JobContext {

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
    private Status status;

    /**
     * 此分发执行此作业上下文的worker
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String workerId;

    /**
     * 任务属性，不可变。
     */
    @Getter
    private ImmutableJobAttribute jobAttributes;

    public SimpleJobContext(String jobId, String contextId, Status status, String workerId,
                            Map<String, List<String>> attributes,
                            JobContextRepository jobContextRepository) {
        super(jobContextRepository);
        this.jobId = jobId;
        this.contextId = contextId;
        this.status = status;
        this.workerId = workerId;

        this.jobAttributes = new ImmutableJobAttribute(attributes);
    }

}
