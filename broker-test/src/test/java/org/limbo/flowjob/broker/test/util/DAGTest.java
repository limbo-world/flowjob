/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.test.util;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.dag.DAG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Devil
 * @since 2021/8/30
 */
public class DAGTest {

    @Test
    public void testNoRoot() {
        WorkflowJobInfo jobInfo = job("1", Collections.singleton("2"));
        WorkflowJobInfo jobInfo2 = job("2", Collections.singleton("3"));
        WorkflowJobInfo jobInfo3 = job("3", Collections.singleton("1"));

        List<WorkflowJobInfo> jobInfos = new ArrayList<>();
        jobInfos.add(jobInfo);
        jobInfos.add(jobInfo2);
        jobInfos.add(jobInfo3);

        try {
            DAG<WorkflowJobInfo> dag = new DAG<>(jobInfos);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testCyclic() {
        WorkflowJobInfo jobInfo = job("1", Collections.singleton("3"));
        WorkflowJobInfo jobInfo2 = job("2", Collections.singleton("3"));
        WorkflowJobInfo jobInfo3 = job("3", Collections.singleton("4"));
        WorkflowJobInfo jobInfo4 = job("4", Collections.singleton("3"));

        List<WorkflowJobInfo> jobInfos = new ArrayList<>();
        jobInfos.add(jobInfo);
        jobInfos.add(jobInfo2);
        jobInfos.add(jobInfo3);
        jobInfos.add(jobInfo4);

        try {
            DAG<WorkflowJobInfo> dag = new DAG<>(jobInfos);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testNormal() {
        WorkflowJobInfo jobInfo1 = job("1", Sets.newHashSet("3", "6"));
        WorkflowJobInfo jobInfo2 = job("2", Sets.newHashSet("3", "4"));

        WorkflowJobInfo jobInfo6 = job("6", Sets.newHashSet("7"));
        WorkflowJobInfo jobInfo3 = job("3", Sets.newHashSet("5"));
        WorkflowJobInfo jobInfo4 = job("4", Sets.newHashSet("5"));

        WorkflowJobInfo jobInfo5 = job("5", Sets.newHashSet("7", "8"));

        WorkflowJobInfo jobInfo7 = job("7", null);
        WorkflowJobInfo jobInfo8 = job("8", null);

        List<WorkflowJobInfo> jobInfos = new ArrayList<>();
        jobInfos.add(jobInfo1);
        jobInfos.add(jobInfo2);
        jobInfos.add(jobInfo3);
        jobInfos.add(jobInfo4);
        jobInfos.add(jobInfo5);
        jobInfos.add(jobInfo6);
        jobInfos.add(jobInfo7);
        jobInfos.add(jobInfo8);

        try {
            DAG<WorkflowJobInfo> dag = new DAG<>(jobInfos);
            WorkflowJobInfo node = dag.getNode("5");
            Verifies.verify(node.getParentIds().size() == 2 && node.getParentIds().containsAll(Sets.newHashSet("3", "4")));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private WorkflowJobInfo job(String id, Set<String> childrenIds) {
        return new WorkflowJobInfo(id, childrenIds);
    }
}
