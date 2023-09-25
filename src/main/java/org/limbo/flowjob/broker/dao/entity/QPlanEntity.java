package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPlanEntity is a Querydsl query type for PlanEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPlanEntity extends EntityPathBase<PlanEntity> {

    private static final long serialVersionUID = -304246976L;

    public static final QPlanEntity planEntity = new QPlanEntity("planEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath appId = createString("appId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath currentVersion = createString("currentVersion");

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final BooleanPath enabled = createBoolean("enabled");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath planId = createString("planId");

    public final StringPath recentlyVersion = createString("recentlyVersion");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPlanEntity(String variable) {
        super(PlanEntity.class, forVariable(variable));
    }

    public QPlanEntity(Path<? extends PlanEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlanEntity(PathMetadata metadata) {
        super(PlanEntity.class, metadata);
    }

}

