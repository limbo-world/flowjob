package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.clent.param.TaskExecuteFeedbackParam;
import org.limbo.flowjob.broker.api.constants.enums.ExecuteResult;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Brozen
 * @since 2022-06-24
 */
@Slf4j
@Service
public class TaskService {

    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepository;

    /**
     * Worker任务执行反馈
     *
     * @param param 反馈参数
     */
    @Transactional
    public void feedback(TaskExecuteFeedbackParam param) {
        // 获取实例
        Task task = taskRepository.get(param.getTaskId());
        Verifies.notNull(task, "Task not exist!");

        ExecuteResult result = param.getResult();
        switch (result) {
            case SUCCEED:
                task.executeSucceed();
                break;

            case FAILED:
                task.executeFail(param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");
        }
    }

}
