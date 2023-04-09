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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.dag.DAGNode;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author Brozen
 * @since 2022-01-05
 */
@Deprecated
public class DagDeserializer extends JsonDeserializer<DAG> {

    @Override
    public DAG deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        List<DAGNode> nodes = JacksonUtils.parseObject(jsonParser.getValueAsString(), new TypeReference<List<DAGNode>>() {
        });
        return new DAG(nodes);
    }

}
