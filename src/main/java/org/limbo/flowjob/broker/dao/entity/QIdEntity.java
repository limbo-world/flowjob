package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QIdEntity is a Querydsl query type for IdEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QIdEntity extends EntityPathBase<IdEntity> {

    private static final long serialVersionUID = 1495267506L;

    public static final QIdEntity idEntity = new QIdEntity("idEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> currentId = createNumber("currentId", Long.class);

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> step = createNumber("step", Integer.class);

    public final StringPath type = createString("type");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QIdEntity(String variable) {
        super(IdEntity.class, forVariable(variable));
    }

    public QIdEntity(Path<? extends IdEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIdEntity(PathMetadata metadata) {
        super(IdEntity.class, metadata);
    }

}

