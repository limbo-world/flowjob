package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPlanSlotEntity is a Querydsl query type for PlanSlotEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPlanSlotEntity extends EntityPathBase<PlanSlotEntity> {

    private static final long serialVersionUID = -1150977954L;

    public static final QPlanSlotEntity planSlotEntity = new QPlanSlotEntity("planSlotEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath planId = createString("planId");

    public final NumberPath<Integer> slot = createNumber("slot", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPlanSlotEntity(String variable) {
        super(PlanSlotEntity.class, forVariable(variable));
    }

    public QPlanSlotEntity(Path<? extends PlanSlotEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlanSlotEntity(PathMetadata metadata) {
        super(PlanSlotEntity.class, metadata);
    }

}

