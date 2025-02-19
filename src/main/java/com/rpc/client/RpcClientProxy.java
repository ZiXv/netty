package com.rpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpc.config.RpcConfig;
import com.rpc.protocol.Header;
import com.rpc.protocol.RpcProtocol;
import com.rpc.protocol.RpcRequest;
import com.rpc.protocol.RpcResponse;
import com.rpc.registry.ServiceDiscovery;
import com.rpc.serialization.ISerializer;
import com.rpc.serialization.SerializerManager;
import io.netty.channel.Channel;
import io.netty.util.internal.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.rpc.protocol.constant.ReqType.REQUEST;
import static com.rpc.protocol.constant.RpcConstant.MAGIC;
import static com.rpc.protocol.constant.RpcConstant.VERSION;

@Slf4j
@Component
public class RpcClientProxy implements InvocationHandler {

    private RpcClient rpcClient;
    private ServiceDiscovery serviceDiscovery;

    private  RpcConfig rpcConfig;

    public RpcClientProxy(RpcClient rpcClient, ServiceDiscovery serviceDiscovery, RpcConfig rpcConfig) {
        this.rpcClient = rpcClient;
        this.serviceDiscovery = serviceDiscovery;
        this.rpcConfig = rpcConfig;
    }




    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 1. 构造 RPC 请求
        RpcRequest request = new RpcRequest();
        request.setServiceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParams(args);
        request.setParameterTypes(Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray(String[]::new));
        request.setAttachments(new HashMap<>());

        // 2. 通过服务发现获取可用服务地址
        List<String> availableServices = serviceDiscovery.getAllServiceAddresses(request.getServiceName());
        log.info(request.getServiceName());
        if (availableServices.isEmpty()) {
            throw new RuntimeException("无可用的 RPC 服务器！");
        }

        // 负载均衡：随机选择一个可用的服务地址
        String targetAddress = availableServices.get(ThreadLocalRandom.current().nextInt(availableServices.size()));
        String[] hostPort = targetAddress.split(":");
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);

        log.info(" 选择的 RPC 服务器地址: {}:{}", host, port);

        // 3. 通过 RpcClient 连接服务器
        rpcClient.connect(host, port);
        Channel channel = rpcClient.getChannel();
        if (channel == null || !channel.isActive()) {
            throw new RuntimeException("无法连接 RPC 服务器 " + host + ":" + port);
        }

        byte serialType = rpcConfig.getSerialType().code();

        int requestLength = calculateSerializedLength(request, serialType);

        UUID uuid = UUID.randomUUID();
        long msgId = uuid.getMostSignificantBits();
        Header header = new Header(
                MAGIC,
                VERSION ,
                serialType,
                REQUEST.code(),
                msgId,
                requestLength
        );

        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>(header, request);
        CompletableFuture<RpcResponse> future = rpcClient.sendRequest(protocol);
        if (method.getReturnType().equals(CompletableFuture.class)) {
            return future.thenApply(response -> {
                if (response.getMsg() != null && !response.getMsg().isEmpty()) {
                    throw new RuntimeException(response.getMsg());
                }
                return response.getData();  // 返回正常数据
            });
        }

        throw new UnsupportedOperationException("RPC 方法必须返回 CompletableFuture<T>，但 "
                + method.getName() + " 返回的是 " + method.getReturnType());
    }

    private int calculateSerializedLength(RpcRequest request, byte serialType) {
        try {
            ISerializer serializer = SerializerManager.getSerializer(serialType);
            byte[] serializedData = serializer.serialize(request);
            return serializedData.length;
        } catch (Exception e) {
            throw new RuntimeException("序列化RpcRequest失败", e);
        }
    }

}
