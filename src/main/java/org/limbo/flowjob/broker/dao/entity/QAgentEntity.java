package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QAgentEntity is a Querydsl query type for AgentEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAgentEntity extends EntityPathBase<AgentEntity> {

    private static final long serialVersionUID = 269410036L;

    public static final QAgentEntity agentEntity = new QAgentEntity("agentEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath agentId = createString("agentId");

    public final NumberPath<Integer> availableQueueLimit = createNumber("availableQueueLimit", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final BooleanPath enabled = createBoolean("enabled");

    public final StringPath host = createString("host");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastHeartbeatAt = createDateTime("lastHeartbeatAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> port = createNumber("port", Integer.class);

    public final StringPath protocol = createString("protocol");

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAgentEntity(String variable) {
        super(AgentEntity.class, forVariable(variable));
    }

    public QAgentEntity(Path<? extends AgentEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAgentEntity(PathMetadata metadata) {
        super(AgentEntity.class, metadata);
    }

}

