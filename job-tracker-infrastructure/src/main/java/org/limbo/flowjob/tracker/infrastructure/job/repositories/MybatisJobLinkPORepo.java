//package org.limbo.flowjob.tracker.infrastructure.job.repositories;
//
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import org.apache.commons.collections4.CollectionUtils;
//import org.limbo.flowjob.tracker.dao.mybatis.JobLinkMapper;
//import org.limbo.flowjob.tracker.dao.po.JobLinkPO;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * @author Devil
// * @since 2021/7/24
// */
//@Repository
//public class MybatisJobLinkPORepo implements JobLinkPORepository {
//
//    @Autowired
//    private JobLinkMapper jobLinkMapper;
//
//    @Override
//    public List<String> getParentJobIds(String jobId, Integer version) {
//        List<JobLinkPO> jobLinkPOS = jobLinkMapper.selectList(Wrappers.<JobLinkPO>lambdaQuery()
//                .eq(JobLinkPO::getJobId, jobId)
//                .eq(JobLinkPO::getVersion, version)
//        );
//        if (CollectionUtils.isEmpty(jobLinkPOS)) {
//            return null;
//        }
//        return jobLinkPOS.stream().map(JobLinkPO::getParentJobId).collect(Collectors.toList());
//    }
//
//    @Override
//    public List<String> getChildrenJobIds(String jobId, Integer version) {
//        List<JobLinkPO> jobLinkPOS = jobLinkMapper.selectList(Wrappers.<JobLinkPO>lambdaQuery()
//                .eq(JobLinkPO::getParentJobId, jobId)
//                .eq(JobLinkPO::getVersion, version)
//        );
//        if (CollectionUtils.isEmpty(jobLinkPOS)) {
//            return null;
//        }
//        return jobLinkPOS.stream().map(JobLinkPO::getJobId).collect(Collectors.toList());
//    }
//}
