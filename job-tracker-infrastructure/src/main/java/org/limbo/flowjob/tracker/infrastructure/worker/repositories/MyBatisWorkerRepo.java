package org.limbo.flowjob.tracker.infrastructure.worker.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.limbo.flowjob.tracker.dao.mybatis.WorkerMapper;
import org.limbo.flowjob.tracker.dao.po.WorkerPO;
import org.limbo.flowjob.tracker.infrastructure.worker.converters.WorkerPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class MyBatisWorkerRepo implements WorkerRepository {

    @Autowired
    private WorkerMapper mapper;

    @Autowired
    private WorkerPoConverter converter;

    /**
     * {@inheritDoc}
     * @param worker worker节点
     */
    @Override
    public void addWorker(Worker worker) {
        WorkerPO po = converter.convert(worker);
        Objects.requireNonNull(po);

        int effected = mapper.insertIgnore(po);
        if (effected <= 0) {
            effected = mapper.update(po, Wrappers.<WorkerPO>lambdaUpdate()
                    .eq(WorkerPO::getWorkerId, po.getWorkerId()));

            if (effected != 1) {
                throw new IllegalStateException(String.format("Update worker error, effected %s rows", effected));
            }
        }
    }

    /**
     * {@inheritDoc}
     * @param worker 更新worker
     */
    @Override
    public void updateWorker(Worker worker) {
        WorkerPO po = converter.convert(worker);
        Objects.requireNonNull(po);

        mapper.update(po, Wrappers.<WorkerPO>lambdaUpdate()
                .eq(WorkerPO::getWorkerId, po.getWorkerId()));
    }

    /**
     * {@inheritDoc}
     * @param workerId workerId
     * @return
     */
    @Override
    public Worker getWorker(String workerId) {
        return converter.reverse().convert(mapper.selectById(workerId));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<Worker> availableWorkers() {
        return mapper.selectList(Wrappers.<WorkerPO>lambdaQuery()
                .eq(WorkerPO::getStatus, WorkerStatus.RUNNING.status)
                .eq(WorkerPO::getDeleted, Boolean.FALSE)
        ).stream()
                .map(po -> converter.reverse().convert(po))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * @param workerId 需要被移除的workerId
     */
    @Override
    public void removeWorker(String workerId) {
        WorkerPO po = mapper.selectById(workerId);
        if (po != null) {
            po.setDeleted(true);
            mapper.updateById(po);
        }
    }
}
