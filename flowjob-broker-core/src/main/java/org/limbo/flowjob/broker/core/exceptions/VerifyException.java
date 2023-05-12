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

package org.limbo.flowjob.broker.core.exceptions;

import java.util.function.Supplier;

/**
 * @author Brozen
 * @since 1.0
 */
public class VerifyException extends RuntimeException {

    private static final long serialVersionUID = 7400987208065883678L;

    public VerifyException(String message) {
        super(message);
    }

    public VerifyException(String message, Throwable cause) {
        super(message, cause);
    }

    public static Supplier<VerifyException> supplier(String message) {
        return () -> new VerifyException(message);
    }

    public static Supplier<VerifyException> supplier(String message, Throwable cause) {
        return () -> new VerifyException(message, cause);
    }
}
