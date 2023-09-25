package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPlanInfoEntity is a Querydsl query type for PlanInfoEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPlanInfoEntity extends EntityPathBase<PlanInfoEntity> {

    private static final long serialVersionUID = -1448726770L;

    public static final QPlanInfoEntity planInfoEntity = new QPlanInfoEntity("planInfoEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobInfo = createString("jobInfo");

    public final StringPath name = createString("name");

    public final StringPath planId = createString("planId");

    public final StringPath planInfoId = createString("planInfoId");

    public final NumberPath<Integer> planType = createNumber("planType", Integer.class);

    public final StringPath scheduleCron = createString("scheduleCron");

    public final StringPath scheduleCronType = createString("scheduleCronType");

    public final NumberPath<Long> scheduleDelay = createNumber("scheduleDelay", Long.class);

    public final DateTimePath<java.time.LocalDateTime> scheduleEndAt = createDateTime("scheduleEndAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> scheduleInterval = createNumber("scheduleInterval", Long.class);

    public final DateTimePath<java.time.LocalDateTime> scheduleStartAt = createDateTime("scheduleStartAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> scheduleType = createNumber("scheduleType", Integer.class);

    public final NumberPath<Integer> triggerType = createNumber("triggerType", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPlanInfoEntity(String variable) {
        super(PlanInfoEntity.class, forVariable(variable));
    }

    public QPlanInfoEntity(Path<? extends PlanInfoEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlanInfoEntity(PathMetadata metadata) {
        super(PlanInfoEntity.class, metadata);
    }

}

