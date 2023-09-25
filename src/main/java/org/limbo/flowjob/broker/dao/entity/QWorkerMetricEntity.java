package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QWorkerMetricEntity is a Querydsl query type for WorkerMetricEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QWorkerMetricEntity extends EntityPathBase<WorkerMetricEntity> {

    private static final long serialVersionUID = 1069350693L;

    public static final QWorkerMetricEntity workerMetricEntity = new QWorkerMetricEntity("workerMetricEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<Float> availableCpu = createNumber("availableCpu", Float.class);

    public final NumberPath<Integer> availableQueueLimit = createNumber("availableQueueLimit", Integer.class);

    public final NumberPath<Long> availableRam = createNumber("availableRam", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastHeartbeatAt = createDateTime("lastHeartbeatAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath workerId = createString("workerId");

    public QWorkerMetricEntity(String variable) {
        super(WorkerMetricEntity.class, forVariable(variable));
    }

    public QWorkerMetricEntity(Path<? extends WorkerMetricEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWorkerMetricEntity(PathMetadata metadata) {
        super(WorkerMetricEntity.class, metadata);
    }

}

