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

package org.limbo.flowjob.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author brozen
 * @since 2022-09-21
 */
@Slf4j
public class SHAUtils {


    /**
     * 进行SHA-512加密 Hex(SHA-512(data))
     * @param data 待加密数据
     * @return 加密后base64的数据
     */
    public static String sha1AndHex(String data) {
        return sha1AndHex(data.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 进行SHA-512加密 Base64(SHA-512(data))
     * @param data 待加密数据
     * @return 加密后base64的数据
     */
    public static String sha1AndHex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] md5Encrypted = digest.digest(data);
            return toHex(md5Encrypted).toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA1加密异常！", e);
            throw new IllegalStateException("SHA1加密异常！", e);
        }
    }


    /**
     * byte 数组转为 16 进制形式
     */
    private static StringBuilder toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String h = Integer.toHexString(b & 0xFF);
            if (h.length() == 1) {
                hex.append('0');
            }
            hex.append(h);
        }
        return hex;
    }

}
