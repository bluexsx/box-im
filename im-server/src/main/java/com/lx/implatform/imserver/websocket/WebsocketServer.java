package com.lx.implatform.imserver.websocket;

import com.lx.implatform.imserver.websocket.endecode.MessageProtocolDecoder;
import com.lx.implatform.imserver.websocket.endecode.MessageProtocolEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WebsocketServer {

        public static String LOCAL_SERVER_ID = UUID.randomUUID().toString();

        public void start(int port) {
            // 服务端启动辅助类，用于设置TCP相关参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 获取Reactor线程池
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workGroup = new NioEventLoopGroup();
            // 设置为主从线程模型
            bootstrap.group(bossGroup, workGroup)
                    // 设置服务端NIO通信类型
                    .channel(NioServerSocketChannel.class)
                    // 设置ChannelPipeline，也就是业务职责链，由处理的Handler串联而成，由从线程池处理
                    .childHandler(new ChannelInitializer<Channel>() {
                        // 添加处理的Handler，通常包括消息编解码、业务处理，也可以是日志、权限、过滤等
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // 获取职责链
                            ChannelPipeline pipeline = ch.pipeline();
                            //
                            pipeline.addLast(new IdleStateHandler(15, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast("http-codec", new HttpServerCodec());
                            pipeline.addLast("aggregator", new HttpObjectAggregator(65535));
                            pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                            pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                            pipeline.addLast("encode",new MessageProtocolEncoder());
                            pipeline.addLast("decode",new MessageProtocolDecoder());
                            pipeline.addLast("handler", new WebSocketHandler());
                        }
                    })
                    // bootstrap 还可以设置TCP参数，根据需要可以分别设置主线程池和从线程池参数，来优化服务端性能。
                    // 其中主线程池使用option方法来设置，从线程池使用childOption方法设置。
                    // backlog表示主线程池中在套接口排队的最大数量，队列由未连接队列（三次握手未完成的）和已连接队列
                    .option(ChannelOption.SO_BACKLOG, 5)
                    // 表示连接保活，相当于心跳机制，默认为7200s
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            try {
                // 绑定端口，启动select线程，轮询监听channel事件，监听到事件之后就会交给从线程池处理
                Channel channel = bootstrap.bind(port).sync().channel();
                // 等待服务端口关闭
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // 优雅退出，释放线程池资源
                bossGroup.shutdownGracefully();
                workGroup.shutdownGracefully();
            }
        }

}
