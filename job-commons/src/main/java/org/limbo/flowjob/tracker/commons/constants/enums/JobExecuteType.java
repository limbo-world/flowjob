package org.limbo.flowjob.tracker.commons.constants.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 作业执行方式
 *
 * @author Brozen
 * @since 2021-07-05
 */
public enum JobExecuteType {

    /**
     * 非法值
     */
    UNKNOWN(0, "未知执行方式"),

    /**
     * 通过bean执行任务，worker中有能够执行任务的bean，接收到任务后，执行指定bean的逻辑
     */
    BEAN(1, "通过bean执行任务"),

    /**
     * 通过shell执行任务，shell脚本下发到worker后，worker在本地执行脚本
     */
    SHELL(2, "shell脚本任务"),

    ;

    /**
     * 执行方式枚举值
     */
    @JsonValue
    public final byte type;

    /**
     * 执行方式描述
     */
    public final String desc;

    JobExecuteType(int type, String desc) {
        this(((byte) type), desc);
    }

    JobExecuteType(byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    /**
     * 解析作业执行方式。
     * @return 解析得到的作业执行方式枚举，解析失败则返回{@link #UNKNOWN}
     */
    @JsonCreator
    public static JobExecuteType parse(Number type) {
        if (type == null) {
            return null;
        }

        for (JobExecuteType executeType : values()) {
            if (type.byteValue() == executeType.type) {
                return executeType;
            }
        }

        return UNKNOWN;
    }

}
