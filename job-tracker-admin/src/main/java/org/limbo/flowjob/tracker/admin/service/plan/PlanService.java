package org.limbo.flowjob.tracker.admin.service.plan;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.JobNodeType;
import org.limbo.flowjob.tracker.commons.dto.job.DispatchOptionDto;
import org.limbo.flowjob.tracker.commons.dto.job.ExecutorOptionDto;
import org.limbo.flowjob.tracker.commons.dto.job.JobDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanAddDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanReplaceDto;
import org.limbo.flowjob.tracker.commons.dto.plan.ScheduleOptionDto;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.ExecutorOption;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.dag.DAG;
import org.limbo.flowjob.tracker.core.job.dag.DAGNode;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanBuilderFactory;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.plan.ScheduleOption;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.limbo.flowjob.tracker.infrastructure.plan.repositories.PlanPoRepository;
import org.limbo.utils.verifies.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanPoRepository planPoRepository;

    @Autowired
    private TrackerNode trackerNode;

    @Autowired
    private PlanBuilderFactory planBuilderFactory;

    /**
     * 新增计划 只是个落库操作
     */
    public String add(PlanAddDto dto) {
        // 保存 plan
        Plan plan = convertToDo(dto);
        return planRepository.addPlan(plan);
    }

    /**
     * 覆盖计划 可能会触发 内存时间轮改动
     */
    public void replace(String planId, PlanReplaceDto dto) {
        // 获取当前的plan数据
        Plan newVersion = planRepository.newVersion(convertToDo(planId, dto));

        // 需要修改plan重新调度
        if (trackerNode.jobTracker().isScheduling(planId)) {
            trackerNode.jobTracker().unschedule(planId);
            trackerNode.jobTracker().schedule(newVersion);
        }
    }

    /**
     * 启动计划 开始调度 todo 并发
     */
    public void start(String planId) {
        PlanPO planPO = planPoRepository.getById(planId);
        Verifies.notNull(planPO, "plan is not exist");

        if (planPO.getIsEnabled()) {
            return;
        }

        Plan plan = planRepository.getPlan(planId, planPO.getCurrentVersion());
        Verifies.notEmpty(plan.getJobs(), "job is empty");

        // 更新状态
        planPoRepository.switchEnable(planId, true);

        // 调度 plan
        trackerNode.jobTracker().schedule(plan);
    }

    /**
     * 取消计划 停止调度
     */
    public void stop(String planId) {
        PlanPO planPO = planPoRepository.getById(planId);
        Verifies.notNull(planPO, "plan is not exist");

        if (!planPO.getIsEnabled()) {
            return;
        }

        planPoRepository.switchEnable(planId, false);

        // 停止调度 plan
        trackerNode.jobTracker().unschedule(planId);
    }


    private Plan convertToDo(PlanAddDto dto) {
        return planBuilderFactory.newBuilder()
                .planId(dto.getPlanId())
                .version(1)
                .planDesc(dto.getPlanDesc())
                .scheduleOption(convertToDo(dto.getScheduleOption()))
                .jobs(convertToDo(dto.getJobs()))
                .build();
    }

    private Plan convertToDo(String planId, PlanReplaceDto dto) {
        return planBuilderFactory.newBuilder()
                .planId(planId)
                .planDesc(dto.getPlanDesc())
                .scheduleOption(convertToDo(dto.getScheduleOption()))
                .jobs(convertToDo(dto.getJobs()))
                .build();
    }

    private DispatchOption convertToDo(DispatchOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new DispatchOption(dto.getDispatchType(), dto.getCpuRequirement(), dto.getRamRequirement());
    }

    private ExecutorOption convertToDo(ExecutorOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new ExecutorOption(dto.getName(), dto.getType());
    }

    private ScheduleOption convertToDo(ScheduleOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new ScheduleOption(dto.getScheduleType(), dto.getScheduleStartAt(), dto.getScheduleDelay(),
                dto.getScheduleInterval(), dto.getScheduleCron());
    }

    private List<Job> convertToDo(List<JobDto> dtos) {
        List<Job> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(dtos)) {
            return list;
        }

        // 判断 ID 是否相同 是否多个起点，多个终点
        DAGNode dagStart = null;
        DAGNode dagEnd = null;
        Map<String, DAGNode> dagNodes = new HashMap<>();
        for (JobDto dto : dtos) {
            Verifies.verify(!dagNodes.containsKey(dto.getJobId()), "exist same job id:" + dto.getJobId());
            DAGNode dagNode = new DAGNode(dto.getJobId(), dto.getChildrenIds());
            dagNodes.put(dagNode.getId(), dagNode);
            if (JobNodeType.START.toString().equalsIgnoreCase(dto.getNodeType())) {
                Verifies.isNull(dagStart, "DAG start node must be one");
                dagStart = dagNode;
            } else if (JobNodeType.END.toString().equalsIgnoreCase(dto.getNodeType())) {
                Verifies.isNull(dagEnd, "DAG end node must be one");
                dagEnd = dagNode;
            }
        }
        Verifies.notNull(dagStart, "DAG start node must be one");
        Verifies.notNull(dagEnd, "DAG end node must be one");

        // 检测是否成环
        Verifies.verify(!DAG.hasCyclic(dagStart, dagNodes), "there has cyclic in graph!");

        // 封装对象
        for (JobDto dto : dtos) {
            list.add(convertToDo(dto));
        }
        return list;
    }

    private Job convertToDo(JobDto dto) {
        Job job = new Job();
        job.setJobId(dto.getJobId());
        job.setJobDesc(dto.getJobDesc());
//       todo job.setChildrenIds(dto.getChildrenIds());
        job.setDispatchOption(convertToDo(dto.getDispatchOption()));
        job.setExecutorOption(convertToDo(dto.getExecutorOption()));
        return job;
    }


}
