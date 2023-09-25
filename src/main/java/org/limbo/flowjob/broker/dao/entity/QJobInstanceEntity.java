package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QJobInstanceEntity is a Querydsl query type for JobInstanceEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QJobInstanceEntity extends EntityPathBase<JobInstanceEntity> {

    private static final long serialVersionUID = 275967137L;

    public static final QJobInstanceEntity jobInstanceEntity = new QJobInstanceEntity("jobInstanceEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath agentId = createString("agentId");

    public final StringPath context = createString("context");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final DateTimePath<java.time.LocalDateTime> endAt = createDateTime("endAt", java.time.LocalDateTime.class);

    public final StringPath errorMsg = createString("errorMsg");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobId = createString("jobId");

    public final StringPath jobInstanceId = createString("jobInstanceId");

    public final DateTimePath<java.time.LocalDateTime> lastReportAt = createDateTime("lastReportAt", java.time.LocalDateTime.class);

    public final StringPath planId = createString("planId");

    public final StringPath planInfoId = createString("planInfoId");

    public final StringPath planInstanceId = createString("planInstanceId");

    public final NumberPath<Integer> retryTimes = createNumber("retryTimes", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> startAt = createDateTime("startAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> triggerAt = createDateTime("triggerAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QJobInstanceEntity(String variable) {
        super(JobInstanceEntity.class, forVariable(variable));
    }

    public QJobInstanceEntity(Path<? extends JobInstanceEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJobInstanceEntity(PathMetadata metadata) {
        super(JobInstanceEntity.class, metadata);
    }

}

