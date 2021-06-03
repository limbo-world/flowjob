package org.limbo.flowjob.tracker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.limbo.flowjob.tracker.dao.po.WorkerMetricPO;

/**
 * @author Brozen
 * @since 2021-06-02
 */
public interface WorkerMetricMapper extends BaseMapper<WorkerMetricPO> {

    /**
     * 新增worker指标采集数据记录，使用insert ignore语句
     * @param metric 指标信息
     * @return 影响的行数
     */
    int insertIgnore(WorkerMetricPO metric);

}
