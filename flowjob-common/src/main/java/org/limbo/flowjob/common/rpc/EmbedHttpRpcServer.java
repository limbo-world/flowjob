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

package org.limbo.flowjob.common.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Devil
 * @since 2023/8/10
 */
@Slf4j
public class EmbedHttpRpcServer implements EmbedRpcServer {

    private Thread thread;

    private static final int MAX_CONTENT_LENGTH = 5 * 1024 * 1024;

    private static final int QUEUE_SIZE = 2000;

    private int port;

    private IHttpHandlerProcessor bizProcess;

    @Getter
    private AtomicReference<RpcServerStatus> status;

    public EmbedHttpRpcServer(int port, IHttpHandlerProcessor bizProcess) {
        this.port = port;
        this.bizProcess = bizProcess;
        this.status = new AtomicReference<>(RpcServerStatus.IDLE);
    }

    public void start() {
        thread = new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            ThreadPoolExecutor serverThreadPool = new ThreadPoolExecutor(
                    0,
                    200,
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(QUEUE_SIZE),
                    r -> new Thread(r, "Flowjob-RpcServer-ThreadPool-" + r.hashCode()),
                    (r, executor) -> {
                        throw new RuntimeException("Http Server ThreadPool is EXHAUSTED!");
                    });


            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel channel) {
                                channel.pipeline()
                                        .addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
                                        .addLast(new HttpServerCodec())
                                        .addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH))
                                        .addLast(new EmbedHttpServerHandler(serverThreadPool, bizProcess));
                            }
                        })
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture future = bootstrap.bind(port).sync();

                status.compareAndSet(RpcServerStatus.INITIALIZING, RpcServerStatus.RUNNING);
                log.info("Flowjob EmbedRpcServer start success, port = {}", port);

                // 绑定监听关闭状态 -- 阻塞
                future.channel().closeFuture().sync();

            } catch (Exception e) {
                log.error("EmbedHttpRpcServer error", e);
                throw new RuntimeException(e);
            } finally {
                status.compareAndSet(RpcServerStatus.RUNNING, RpcServerStatus.TERMINATING);
                // stop
                try {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                } catch (Exception e) {
                    log.error("EmbedHttpRpcServer stop fail", e);
                }
                status.compareAndSet(RpcServerStatus.RUNNING, RpcServerStatus.TERMINATED);
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        status.compareAndSet(RpcServerStatus.RUNNING, RpcServerStatus.TERMINATING);

        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }

        status.compareAndSet(RpcServerStatus.RUNNING, RpcServerStatus.TERMINATED);
    }

}
