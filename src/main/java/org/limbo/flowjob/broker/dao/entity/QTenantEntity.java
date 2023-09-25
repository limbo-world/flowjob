package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QTenantEntity is a Querydsl query type for TenantEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTenantEntity extends EntityPathBase<TenantEntity> {

    private static final long serialVersionUID = 1778754305L;

    public static final QTenantEntity tenantEntity = new QTenantEntity("tenantEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath secret = createString("secret");

    public final StringPath tenantId = createString("tenantId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTenantEntity(String variable) {
        super(TenantEntity.class, forVariable(variable));
    }

    public QTenantEntity(Path<? extends TenantEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTenantEntity(PathMetadata metadata) {
        super(TenantEntity.class, metadata);
    }

}

