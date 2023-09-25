package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPlanInstanceEntity is a Querydsl query type for PlanInstanceEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPlanInstanceEntity extends EntityPathBase<PlanInstanceEntity> {

    private static final long serialVersionUID = -132391115L;

    public static final QPlanInstanceEntity planInstanceEntity = new QPlanInstanceEntity("planInstanceEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath attributes = createString("attributes");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final DateTimePath<java.time.LocalDateTime> feedbackAt = createDateTime("feedbackAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath planId = createString("planId");

    public final StringPath planInfoId = createString("planInfoId");

    public final StringPath planInstanceId = createString("planInstanceId");

    public final NumberPath<Integer> scheduleType = createNumber("scheduleType", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> startAt = createDateTime("startAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> triggerAt = createDateTime("triggerAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> triggerType = createNumber("triggerType", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPlanInstanceEntity(String variable) {
        super(PlanInstanceEntity.class, forVariable(variable));
    }

    public QPlanInstanceEntity(Path<? extends PlanInstanceEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlanInstanceEntity(PathMetadata metadata) {
        super(PlanInstanceEntity.class, metadata);
    }

}

