package org.limbo.flowjob.tracker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.limbo.flowjob.tracker.dao.po.JobPO;

/**
 * @author Brozen
 * @since 2021-06-02
 */
public interface JobMapper extends BaseMapper<JobPO> {

    /**
     * 插入Job记录，通过 insert ignore into 语句实现。
     * @param job 需要新增的Job
     * @return 影响的记录条数
     */
    int insertIgnore(JobPO job);

}
