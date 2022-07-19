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

package org.limbo.flowjob.broker.cluster;

import org.limbo.flowjob.broker.api.dto.BrokerDTO;
import org.limbo.flowjob.broker.core.utils.TimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 基于db的发布订阅
 *
 * @author Devil
 * @since 2022/7/15
 */
public class DBBrokerRegistry implements BrokerRegistry {

    private final BrokerRepository brokerRepository;

    public DBBrokerRegistry(BrokerRepository brokerRepository) {
        this.brokerRepository = brokerRepository;
    }

    @Override
    public void register(String host, int port) {

        Long HEARTBEAT_TIMEOUT = Constants.HEARTBEAT_TIMEOUT / 1000;

        // 开启定时任务 维持心跳
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                brokerRepository.heartbeat(host, port);
            }
        }, 0, Constants.HEARTBEAT_INTERVAL);

        // 开启定时任务 reload plan
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LocalDateTime localDateTime = TimeUtil.nowLocalDateTime();
                localDateTime = localDateTime.plusSeconds(HEARTBEAT_TIMEOUT);
                List<BrokerDTO> brokers = brokerRepository.online(localDateTime);
                // 要判断自己是否在，不在的话得停止本机的调度
                // 负载：根据id排序 根据broker数量以及自己的序号 根据槽位大小进行hash 重新加载自己槽位下的plan
                // 根据槽位获取plan(下次触发时间在10分钟内---所以如果节点重启会导致丢失10分钟任务) 立即更新plan的下次触发时间 更新成功后放入时间轮
                // 如果存在非自己槽位下的任务 需要清理时间轮中的plan


                // 防止重复下发：
                // 如果存在非自己槽位下的任务 需要清理时间轮中的plan
                // 如果是固定间隔的任务 plan要有完成时间和下次下发时间等 broker应该是需要固定间隔去获取比如10分钟内需要触发的任务
                // 获取到db任务后创建planinstance（防止丢失任务对于间隔比较大的任务来说），然后丢到时间轮中，
                // 等到触发时间到了，丢到另一个线程中去创建task(ps)和更新plan的下次触发时间
                // 创建planinstance的时候增加调度时间，表示本次触发是什么时候，如果下次创建发现已经存在同时间的非手动创建的planinstacen，则不再创建（防止重复任务）
                // ps: 这里创建的时候也需要判断对应planinstance的是否已经存在了（防止重复）
                // 假设 A 创建pi1 放到内存 然后心跳超时（正常来说不可能，因为如果心跳超时的话这个机器应该连不上db创建pi数据，除非心跳的线程挂了）
                // B 分配到plan获取到pi1 放到内存
            }
        }, 0, Constants.REBALANCE_INTERVAL);
    }

    @Override
    public void subscribe(BrokerNodeListener listener) {

    }

}
