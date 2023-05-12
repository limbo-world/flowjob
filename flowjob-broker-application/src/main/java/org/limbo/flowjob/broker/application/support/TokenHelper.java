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

package org.limbo.flowjob.broker.application.support;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

/**
 * @author Devil
 * @since 2022/11/2
 */
public class TokenHelper {

    private static final String ISSUER = "flowjob";

    private static final String WORKER_ID = "workerId";

    public static String workerToken(String workerId, String secret, Date expiresAt) {
        return JWT.create().withIssuer(ISSUER)
                .withClaim(WORKER_ID, workerId)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secret));
    }



}
