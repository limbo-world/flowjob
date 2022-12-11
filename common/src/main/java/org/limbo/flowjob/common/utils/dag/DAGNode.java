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

package org.limbo.flowjob.common.utils.dag;

import java.io.Serializable;
import java.util.Set;

public abstract class DAGNode implements Serializable {

    private static final long serialVersionUID = 8572090796475782411L;

    protected Set<String> parentIds;

    protected Set<String> childrenIds;

    protected int status = DAG.STATUS_INIT;

    public abstract String getId();

    public Set<String> getParentIds() {
        return parentIds;
    }

    public Set<String> getChildrenIds() {
        return childrenIds;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void addParent(String childrenId) {
        parentIds.add(childrenId);
    }

}
