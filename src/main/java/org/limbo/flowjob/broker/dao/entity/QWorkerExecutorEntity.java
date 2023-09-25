package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QWorkerExecutorEntity is a Querydsl query type for WorkerExecutorEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QWorkerExecutorEntity extends EntityPathBase<WorkerExecutorEntity> {

    private static final long serialVersionUID = -1760606328L;

    public static final QWorkerExecutorEntity workerExecutorEntity = new QWorkerExecutorEntity("workerExecutorEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath workerExecutorId = createString("workerExecutorId");

    public final StringPath workerId = createString("workerId");

    public QWorkerExecutorEntity(String variable) {
        super(WorkerExecutorEntity.class, forVariable(variable));
    }

    public QWorkerExecutorEntity(Path<? extends WorkerExecutorEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWorkerExecutorEntity(PathMetadata metadata) {
        super(WorkerExecutorEntity.class, metadata);
    }

}

