package com.rpc.client;

import com.rpc.api.HelloService;
import com.rpc.protocol.*;
import com.rpc.registry.ZkServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RpcClient {
    private final ZkServiceDiscovery serviceDiscovery;

    private final RpcClientProxyFactory proxyFactory;
    private final EventLoopGroup group;
    private volatile Channel channel;

    private volatile HelloService helloService;
    private RpcClientHandler clientHandler;

    @Autowired
    public RpcClient(ZkServiceDiscovery serviceDiscovery,  RpcClientProxyFactory proxyFactory) {
        this.serviceDiscovery = serviceDiscovery;
        this.proxyFactory = proxyFactory;
        this.group = new NioEventLoopGroup();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startClient() {
        log.info("RPC Client is starting...");
        new Thread(this::discoverAndConnect).start();
    }

    private synchronized void discoverAndConnect() {

            String serviceName = HelloService.class.getName();
            List<String> availableServices = serviceDiscovery.getAllServiceAddresses(serviceName);

            if (availableServices.isEmpty()) {
                log.error("无可用的 RPC 服务器！");
                return;
            }

            for (String targetAddress : availableServices) {
                String[] hostPort = targetAddress.split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);

                if (connect(host, port)) {
                    log.info("RPC Client 连接成功: {}:{}", host, port);

                    //返回一个代理类实例
                    this.helloService = proxyFactory.createProxy(HelloService.class);
                    log.info("HelloService代理对象已创建，准备进行远程调用！");
                    callHelloService(); // 连接成功后调用
                    return;
                }
            }
            log.error("所有 RPC 服务器连接失败！");
        }



    public synchronized boolean connect(String host, int port) {
        if (channel != null && channel.isActive()) {
            log.info("已连接到 {}:{}，无需重新连接", host, port);
            return true;
        }
        log.info("连接到 RPC 服务器 {}:{}...", host, port);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(new RpcEncoder());
                            ch.pipeline().addLast(new RpcDecoder());
                            ch.pipeline().addLast(new IdleStateHandler(3, 0, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new HeartbeatHandler());  // 处理心跳
                            RpcClientHandler tmp = new RpcClientHandler();
                            ch.pipeline().addLast(tmp);
                            clientHandler = tmp;
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            this.channel = future.channel();
            return true;
        } catch (Exception e) {
            log.error("无法连接 RPC 服务器 {}:{}", host, port, e);
            return false;
        }
    }
    public CompletableFuture<RpcResponse> sendRequest(RpcProtocol<RpcRequest> protocol) {
        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException("Channel 不可用，尚未连接服务端或已关闭");
        }
        // 直接调用 handler
        return clientHandler.sendRpcRequest(channel, protocol);
    }
    private void callHelloService() {
        if (helloService == null) {
            log.error("HelloService代理对象未初始化，无法进行 RPC 调用");
            return;
        }
        CompletableFuture<String> future = helloService.sayHello("Alice");

        future.thenAccept(response -> log.info("RPC Response: {}", response))
                .exceptionally(e -> {
                    log.error("RPC 调用失败: {}", e.getMessage());
                    return null;
                });
    }

    @PreDestroy
    public void shutdown() {
        log.info("关闭 RPC 客户端...");
        if (channel != null) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                log.error("关闭 Netty 通道时发生错误", e);
            }
        }
        group.shutdownGracefully();
        log.info("RPC 客户端已关闭");
    }

    public Channel getChannel() {
        return channel;
    }
}
