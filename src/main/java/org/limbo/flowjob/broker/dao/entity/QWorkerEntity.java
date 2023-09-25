package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QWorkerEntity is a Querydsl query type for WorkerEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QWorkerEntity extends EntityPathBase<WorkerEntity> {

    private static final long serialVersionUID = -584802603L;

    public static final QWorkerEntity workerEntity = new QWorkerEntity("workerEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath appId = createString("appId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final BooleanPath enabled = createBoolean("enabled");

    public final StringPath host = createString("host");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> port = createNumber("port", Integer.class);

    public final StringPath protocol = createString("protocol");

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath workerId = createString("workerId");

    public QWorkerEntity(String variable) {
        super(WorkerEntity.class, forVariable(variable));
    }

    public QWorkerEntity(Path<? extends WorkerEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWorkerEntity(PathMetadata metadata) {
        super(WorkerEntity.class, metadata);
    }

}

