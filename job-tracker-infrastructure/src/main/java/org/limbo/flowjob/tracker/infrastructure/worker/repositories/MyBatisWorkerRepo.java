package org.limbo.flowjob.tracker.infrastructure.worker.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerDO;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.limbo.flowjob.tracker.dao.mybatis.WorkerMapper;
import org.limbo.flowjob.tracker.dao.po.WorkerPO;
import org.limbo.flowjob.tracker.infrastructure.worker.converters.WorkerPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

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
    public void addWorker(WorkerDO worker) {
        WorkerPO po = converter.convert(worker);
        // TODO
    }

    /**
     * {@inheritDoc}
     * @param worker 更新worker
     */
    @Override
    public void updateWorker(WorkerDO worker) {
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
    public WorkerDO getWorker(String workerId) {
        return converter.reverse().convert(mapper.selectById(workerId));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<WorkerDO> availableWorkers() {
        // TODO
        return null;
    }

    /**
     * {@inheritDoc}
     * @param workerId 需要被移除的workerId
     */
    @Override
    public void removeWorker(String workerId) {
        // TODO
    }
}
