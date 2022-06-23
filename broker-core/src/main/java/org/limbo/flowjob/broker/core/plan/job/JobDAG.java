package org.limbo.flowjob.broker.core.plan.job;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.utils.Verifies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2021/8/18
 */
public class JobDAG {

    private static final int STATE_INIT = 0;
    /**
     * 当遍历的时候第二次进入某个节点，表示成环
     */
    private static final int STATE_VISITED = 1;
    /**
     * 当一个节点 所有子节点都已经被遍历 而且没有环
     * 则它的后继判断也可以省略了
     * 因为如果要形成环，必定是会访问之前已访问的节点
     * 最简单的例子：有环列表
     */
    private static final int STATE_FILTER = 2;

    /**
     * job映射关系
     */
    private Map<String, Job> jobs;

    /**
     * 根
     */
    private Set<String> roots;

    /**
     * 叶子
     */
    private Set<String> leaf;

    /**
     * 节点映射关系
     */
    private Map<String, DAGNode> nodes;

    public JobDAG(List<Job> jobs) {
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }

        this.jobs = new HashMap<>();
        this.nodes = new HashMap<>();
        this.roots = new HashSet<>();
        this.leaf = new HashSet<>();

        init(jobs);
    }

    private void init(List<Job> jobsList) {
        // 数据初始化
        jobsList.forEach(job -> {
            // todo 判断每个job数据是否正确
            jobs.put(job.getJobId(), job);
            nodes.put(job.getJobId(), new DAGNode(job.getJobId(), job.getChildrenIds()));
        });
        jobsList.forEach(job -> {
            if (CollectionUtils.isEmpty(job.getChildrenIds())) {
                return;
            }
            // 判断childrenId是否都有对应job 设置父id
            for (String childrenId : job.getChildrenIds()) {
                DAGNode node = nodes.get(childrenId);
                Verifies.notNull(node, "job " + job.getJobId() + " child " + childrenId + " is not exist");
                node.addParent(job.getJobId());
            }
        });

        // 获取 根节点（没有其它节点指向的节点）叶子节点（没有子节点的）
        roots = jobsList.stream().map(Job::getJobId).collect(Collectors.toSet());
        jobsList.forEach(job -> {
            if (CollectionUtils.isEmpty(job.getChildrenIds())) {
                leaf.add(job.getJobId());
            } else {
                for (String childrenId : job.getChildrenIds()) {
                    roots.remove(childrenId);
                }
            }
        });
        Verifies.notEmpty(roots, "root nodes ie empty");
        Verifies.notEmpty(leaf, "leaf nodes ie empty");

        // 是否有环
        for (String root : roots) {
            Verifies.verify(!hasCyclic(nodes.get(root)), "jobs has cyclic");
        }
    }

    public Job getJob(String jobId) {
        return jobs.get(jobId);
    }

    /**
     * 获取最终执行的job
     *
     * @return 最终执行的 job
     */
    public List<Job> getFinalJobs() {
        List<Job> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(leaf)) {
            return result;
        }
        for (String id : leaf) {
            result.add(jobs.get(id));
        }
        return result;
    }

    /**
     * 获取最先需要执行的job 因为 DAG 可能会有多个一起执行
     *
     * @return 最先执行的 job
     */
    public List<Job> getEarliestJobs() {
        List<Job> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(roots)) {
            return result;
        }
        for (String root : roots) {
            result.add(jobs.get(root));
        }
        return result;
    }


    /**
     * 获取job后续的作业
     */
    public List<Job> getSubJobs(String jobId) {
        List<Job> result = new ArrayList<>();
        Job job = jobs.get(jobId);
        if (CollectionUtils.isEmpty(job.getChildrenIds())) {
            return result;
        }
        for (String childrenId : job.getChildrenIds()) {
            result.add(jobs.get(childrenId));
        }
        return result;
    }

    /**
     * 获取job前置的作业
     */
    public List<Job> getPreJobs(String jobId) {
        List<Job> result = new ArrayList<>();
        DAGNode node = nodes.get(jobId);
        if (CollectionUtils.isEmpty(node.getParentIds())) {
            return result;
        }
        for (String parentId : node.getParentIds()) {
            result.add(jobs.get(parentId));
        }
        return result;
    }

    /**
     * 深度优先搜索
     *
     * @param node
     * @return
     */
    public boolean hasCyclic(DAGNode node) {
        // 表示当前节点已被标记
        node.setState(STATE_VISITED);
        // 如果不存在子节点 则表示此顶点不再有出度 返回父节点
        if (CollectionUtils.isNotEmpty(node.getChildrenIds())) {
            // 遍历子节点
            for (String childId : node.getChildrenIds()) {
                DAGNode child = nodes.get(childId);
                if (child == null || STATE_FILTER == child.getState()) {
                    continue;
                }
                if (STATE_VISITED == child.getState()) {
                    return true;
                }
                if (hasCyclic(child)) {
                    return true;
                }
            }
        }
        node.setState(STATE_FILTER);
        return false;
    }

    public List<Job> jobs() {
        return new ArrayList<>(jobs.values());
    }

    @Data
    public static class DAGNode {

        private String id;

        private Set<String> parentIds;

        private Set<String> childrenIds;
        /**
         * 状态 0 初始-未访问 1 已访问
         */
        private int state;

        public DAGNode(String id, Set<String> childrenIds) {
            this.id = id;
            this.childrenIds = childrenIds;
            this.parentIds = new HashSet<>();
        }

        public void addParent(String childrenId) {
            parentIds.add(childrenId);
        }

    }

}
