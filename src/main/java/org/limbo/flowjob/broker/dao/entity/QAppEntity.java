package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QAppEntity is a Querydsl query type for AppEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAppEntity extends EntityPathBase<AppEntity> {

    private static final long serialVersionUID = -1548792816L;

    public static final QAppEntity appEntity = new QAppEntity("appEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath ak = createString("ak");

    public final StringPath appId = createString("appId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath sk = createString("sk");

    public final StringPath tenantId = createString("tenantId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAppEntity(String variable) {
        super(AppEntity.class, forVariable(variable));
    }

    public QAppEntity(Path<? extends AppEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAppEntity(PathMetadata metadata) {
        super(AppEntity.class, metadata);
    }

}

