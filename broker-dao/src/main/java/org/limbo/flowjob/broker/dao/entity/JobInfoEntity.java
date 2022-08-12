///*
// *
// *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
// *  *
// *  * Licensed under the Apache License, Version 2.0 (the "License");
// *  * you may not use this file except in compliance with the License.
// *  * You may obtain a copy of the License at
// *  *
// *  * 	http://www.apache.org/licenses/LICENSE-2.0
// *  *
// *  * Unless required by applicable law or agreed to in writing, software
// *  * distributed under the License is distributed on an "AS IS" BASIS,
// *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  * See the License for the specific language governing permissions and
// *  * limitations under the License.
// *
// */
//
//package org.limbo.flowjob.broker.dao.entity;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.annotations.DynamicInsert;
//import org.hibernate.annotations.DynamicUpdate;
//import org.limbo.flowjob.broker.api.constants.enums.JobType;
//
//import javax.persistence.Entity;
//import javax.persistence.Table;
//import java.math.BigDecimal;
//
///**
// * plan 中 每个流程节点的具体信息
// *
// * @author Devil
// * @since 2022/6/11
// */
//@Setter
//@Getter
//@Table(name = "flowjob_job_info")
//@Entity
//@DynamicInsert
//@DynamicUpdate
//public class JobInfoEntity extends BaseEntity {
//    private static final long serialVersionUID = -492555252883668183L;
//
//    private Long planInfoId;
//
//    /**
//     * 作业名称
//     */
//    private String name;
//
//    /**
//     * 作业描述
//     */
//    private String description;
//
//    /**
//     * 此作业相连的下级作业ID todo 是否需要再加个关联表查询
//     */
//    private String childrenIds;
//
//    private Byte dispatchType;
//
//    private Byte loadBalanceType;
//
//    private Integer retry;
//
//    private BigDecimal cpuRequirement;
//
//    private BigDecimal ramRequirement;
//
//    private String executorName;
//
//    private Byte executorType;
//
//    /**
//     * @see JobType
//     */
//    protected Byte type;
//}
