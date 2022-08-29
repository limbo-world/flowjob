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

package org.limbo.flowjob.broker.test;

import org.junit.Test;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
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
        JobInfo jobInfo = job("1", Collections.singleton("2"));
        JobInfo jobInfo2 = job("2", Collections.singleton("3"));
        JobInfo jobInfo3 = job("3", Collections.singleton("1"));

        List<JobInfo> jobInfos = new ArrayList<>();
        jobInfos.add(jobInfo);
        jobInfos.add(jobInfo2);
        jobInfos.add(jobInfo3);

        try {
            DAG<JobInfo> dag = new DAG<>(jobInfos);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testCyclic() {
        JobInfo jobInfo = job("1", Collections.singleton("3"));
        JobInfo jobInfo2 = job("2", Collections.singleton("3"));
        JobInfo jobInfo3 = job("3", Collections.singleton("4"));
        JobInfo jobInfo4 = job("4", Collections.singleton("3"));

        List<JobInfo> jobInfos = new ArrayList<>();
        jobInfos.add(jobInfo);
        jobInfos.add(jobInfo2);
        jobInfos.add(jobInfo3);
        jobInfos.add(jobInfo4);

        try {
            DAG<JobInfo> dag = new DAG<>(jobInfos);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private JobInfo job(String id, Set<String> childrenIds) {
        return new JobInfo(id, childrenIds);
    }
}
