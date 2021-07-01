/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.start.bak;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.limbo.flowjob.worker.core.domain.Worker;

/**
 * 与服务端的连接
 *
 * @author Devil
 * @date 2021/6/29 3:33 下午
 */
public class NettyRemoteClient {

    private final String host;
    private final int port;

    private EventLoopGroup bossGroup;

    private Bootstrap bootstrap;

    private ChannelFuture channelFuture;

    private Worker worker;

    public NettyRemoteClient(String host, int port, Worker worker) {
        this.host = host;
        this.port = port;
        this.worker = worker;
    }

    public void start() throws Exception {
        beforeStart();

        clientStart();

        afterStart();
    }

    private void beforeStart() throws Exception {
        this.bossGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();

        bootstrap.group(bossGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        // 处理来自服务端的响应信息
                        socketChannel.pipeline().addLast(new ClientReceiveHandler(worker));
                    }
                });
    }

    private void clientStart() throws Exception {
        // 客户端开启
        this.channelFuture = bootstrap.connect(host, port).sync();
    }

    private void afterStart() throws Exception {
        // 等待直到连接中断
        this.channelFuture.channel().closeFuture().sync();
    }

    public void close() {
        this.bossGroup.shutdownGracefully();
    }

    public Channel getChannel() {
        if (channelFuture == null) {
            return null;
        }
        return channelFuture.channel();
    }


}
