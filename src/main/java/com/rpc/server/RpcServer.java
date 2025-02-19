package com.rpc.server;


import com.rpc.api.HelloService;
import com.rpc.protocol.RpcDecoder;
import com.rpc.protocol.RpcEncoder;
import com.rpc.registry.LocalServiceRegistry;
import com.rpc.registry.ZkServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class RpcServer {
    private final int port;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(10); // 线程池

    private final ZkServiceRegistry zkServiceRegistry;

    private final LocalServiceRegistry localServiceRegistry;
    @Autowired
    public RpcServer(@Value("${rpc.server.port}")int port, ZkServiceRegistry zkServiceRegistry, LocalServiceRegistry localServiceRegistry) {
        this.port = port;
        this.zkServiceRegistry = zkServiceRegistry;
        this.localServiceRegistry = localServiceRegistry;
    }
    @PostConstruct
    public void startServer() {
        new Thread(this::start).start();
        //log.info("RPC Server started successfully on port {}", port);
    }
    @Value("${rpc.server.port}")
    private int rpcServerPort;

    @EventListener(ApplicationReadyEvent.class)
    public void registerServiceOnStartup() {
        String serverAddress = "127.0.0.1:" + rpcServerPort;
        // 比如：HelloService.class.getName() -> "com.rpc.api.HelloService"
        String serviceName = HelloService.class.getName();
        localServiceRegistry.registerService(serviceName, new HelloServiceImpl());
        zkServiceRegistry.registerService(serviceName, serverAddress);
    }


    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(new RpcDecoder());  // 解析请求
                            ch.pipeline().addLast(new RpcEncoder());  // 解析响应
                            ch.pipeline().addLast(new RpcServerHandler(threadPool, localServiceRegistry));  // 处理请求
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("RPC 服务器已启动，监听端口：" + port);
            future.channel().closeFuture().sync();  // 阻塞，直到服务器关闭
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            threadPool.shutdown();
        }
    }
}
