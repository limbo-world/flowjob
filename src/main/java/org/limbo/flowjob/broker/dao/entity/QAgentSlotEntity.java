package org.limbo.flowjob.broker.dao.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QAgentSlotEntity is a Querydsl query type for AgentSlotEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAgentSlotEntity extends EntityPathBase<AgentSlotEntity> {

    private static final long serialVersionUID = -1069560302L;

    public static final QAgentSlotEntity agentSlotEntity = new QAgentSlotEntity("agentSlotEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath agentId = createString("agentId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> slot = createNumber("slot", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAgentSlotEntity(String variable) {
        super(AgentSlotEntity.class, forVariable(variable));
    }

    public QAgentSlotEntity(Path<? extends AgentSlotEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAgentSlotEntity(PathMetadata metadata) {
        super(AgentSlotEntity.class, metadata);
    }

}

