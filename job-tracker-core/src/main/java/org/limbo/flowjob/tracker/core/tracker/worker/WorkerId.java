package org.limbo.flowjob.tracker.core.tracker.worker;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerProtocol;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * worker的唯一标识由 protocol、ip、port组成
 *
 * @author Brozen
 * @since 2021-06-10
 */
@Getter
public class WorkerId {

    /**
     * worker服务使用的通信协议，默认为Http协议。
     */
    private final WorkerProtocol protocol;

    /**
     * worker服务的通信IP
     */
    private final String ip;

    /**
     * worker服务的通信端口
     */
    private final Integer port;

    public WorkerId(WorkerProtocol protocol, String ip, Integer port) {
        this.protocol = protocol;
        this.ip = ip;
        this.port = port;
    }

    /**
     * workerId的字符串形式，格式：{protocol}://{ip}:port
     */
    @Override
    public String toString() {
        return protocol.name + "://" + ip + ":" + port;
    }


    private static final String IP_REGEXP = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";

    private static final String WORKER_ID_REGEXP = "^(?<protocol>[a-zA-Z]+)://(?<ip>" + IP_REGEXP + ")(:(?<port>\\d+))?$";

    private static final Pattern WORKER_ID_PATTERN = Pattern.compile(WORKER_ID_REGEXP);

    /**
     * 从字符串形式解析出WorkerId
     * @param workerId 格式：{protocol}://{ip}:port
     * @return worker唯一ID
     */
    public static WorkerId fromString(String workerId) {
        Matcher matcher = WORKER_ID_PATTERN.matcher(workerId);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("not an illegal workerId string format: " + workerId);
        }

        // 从字符串中解析协议、ip、端口号
        String protocolStr = Objects.requireNonNull(matcher.group("protocol"));
        String ip = Objects.requireNonNull(matcher.group("ip"));
        String portStr = matcher.group("port");

        // 端口号可能未配置，使用默认值
        WorkerProtocol protocol = Objects.requireNonNull(WorkerProtocol.parse(protocolStr));
        int port = protocol.port;
        if (!StringUtils.isBlank(portStr)) {
            port = NumberUtils.createInteger(portStr);
        }

        return new WorkerId(protocol, ip, port);
    }

    public static void main(String[] args) {
        WorkerId workerId = WorkerId.fromString("rs://127.0.0.1");
        System.out.println(workerId);
    }

}
