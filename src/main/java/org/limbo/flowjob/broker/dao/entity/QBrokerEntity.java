package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QBrokerEntity is a Querydsl query type for BrokerEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBrokerEntity extends EntityPathBase<BrokerEntity> {

    private static final long serialVersionUID = 493081328L;

    public static final QBrokerEntity brokerEntity = new QBrokerEntity("brokerEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath brokerId = createString("brokerId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final StringPath host = createString("host");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastHeartbeat = createDateTime("lastHeartbeat", java.time.LocalDateTime.class);

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> onlineTime = createDateTime("onlineTime", java.time.LocalDateTime.class);

    public final NumberPath<Integer> port = createNumber("port", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBrokerEntity(String variable) {
        super(BrokerEntity.class, forVariable(variable));
    }

    public QBrokerEntity(Path<? extends BrokerEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBrokerEntity(PathMetadata metadata) {
        super(BrokerEntity.class, metadata);
    }

}

