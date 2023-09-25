package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QWorkerSlotEntity is a Querydsl query type for WorkerSlotEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QWorkerSlotEntity extends EntityPathBase<WorkerSlotEntity> {

    private static final long serialVersionUID = -1967082125L;

    public static final QWorkerSlotEntity workerSlotEntity = new QWorkerSlotEntity("workerSlotEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> slot = createNumber("slot", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath workerId = createString("workerId");

    public QWorkerSlotEntity(String variable) {
        super(WorkerSlotEntity.class, forVariable(variable));
    }

    public QWorkerSlotEntity(Path<? extends WorkerSlotEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWorkerSlotEntity(PathMetadata metadata) {
        super(WorkerSlotEntity.class, metadata);
    }

}

