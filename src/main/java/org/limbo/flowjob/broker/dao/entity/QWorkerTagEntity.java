package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QWorkerTagEntity is a Querydsl query type for WorkerTagEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QWorkerTagEntity extends EntityPathBase<WorkerTagEntity> {

    private static final long serialVersionUID = 615381995L;

    public static final QWorkerTagEntity workerTagEntity = new QWorkerTagEntity("workerTagEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath tagKey = createString("tagKey");

    public final StringPath tagValue = createString("tagValue");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath workerId = createString("workerId");

    public final StringPath workerTagId = createString("workerTagId");

    public QWorkerTagEntity(String variable) {
        super(WorkerTagEntity.class, forVariable(variable));
    }

    public QWorkerTagEntity(Path<? extends WorkerTagEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWorkerTagEntity(PathMetadata metadata) {
        super(WorkerTagEntity.class, metadata);
    }

}

