package org.limbo.flowjob.tracker.admin.service.plan;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.dto.job.JobDto;
import org.limbo.flowjob.tracker.commons.dto.plan.DispatchOptionDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanAddDto;
import org.limbo.flowjob.tracker.commons.dto.plan.ScheduleOptionDto;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.schedule.DelegatedScheduleCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @date 2021/7/14 5:04 下午
 */
@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    public String add(PlanAddDto dto) {
        return planRepository.addOrUpdatePlan(convertToDo(dto));
    }


    public Plan convertToDo(PlanAddDto dto) {

        ScheduleType scheduleType = dto.getScheduleOption().getScheduleType();
        DelegatedScheduleCalculator delegatedCalculator = new DelegatedScheduleCalculator(scheduleType);

        Plan plan = new Plan(delegatedCalculator);
        plan.setPlanDesc(dto.getPlanDesc());
        plan.setDispatchOption(convertToDo(dto.getDispatchOption()));
        plan.setScheduleOption(convertToDo(dto.getScheduleOption()));
        plan.setJobs(convertToDo(dto.getJobs()));

        return plan;
    }

    public DispatchOption convertToDo(DispatchOptionDto dto) {
        return new DispatchOption(dto.getDispatchType(), dto.getCpuRequirement(), dto.getRamRequirement());
    }

    public ScheduleOption convertToDo(ScheduleOptionDto dto) {
        return new ScheduleOption(dto.getScheduleType(), dto.getScheduleStartAt(), dto.getScheduleDelay(),
                dto.getScheduleInterval(), dto.getScheduleCron());
    }

    public List<Job> convertToDo(List<JobDto> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return null;
        }
        List<Job> list = new ArrayList<>();
        for (JobDto dto : dtos) {
//            list.add(new Job())// todo
        }
        return null;
    }


}
