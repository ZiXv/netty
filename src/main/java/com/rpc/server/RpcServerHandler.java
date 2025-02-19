package com.rpc.server;

import com.rpc.protocol.Header;
import com.rpc.protocol.RpcProtocol;
import com.rpc.protocol.RpcRequest;
import com.rpc.protocol.RpcResponse;
import com.rpc.protocol.constant.ReqType;
import com.rpc.registry.ServiceRegistry;
import com.rpc.registry.ZkServiceRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private final ExecutorService threadPool;

    private final ServiceRegistry serviceRegistry;


    public RpcServerHandler(ExecutorService threadPool, ServiceRegistry serviceRegistry) {
        this.threadPool = threadPool;
        this.serviceRegistry = serviceRegistry;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        byte msgType = protocol.getHeader().getMsgType();

        // **1. 处理心跳包（直接在 I/O 线程处理，不进线程池）**
        if (msgType == ReqType.HEARTBEAT_REQUEST.code()) {
            handleHeartbeat(ctx, protocol);
            return;
        }

        // **2. 业务请求提交到线程池**
        threadPool.submit(() -> handleRequest(ctx, protocol));
    }

    /**
     * 处理心跳请求
     */
    private void handleHeartbeat(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        Header header = protocol.getHeader();
        header.setMsgType(ReqType.HEARTBEAT_RESPONSE.code());

        RpcResponse heartbeatResp = new RpcResponse();
        heartbeatResp.setData("PONG");

        RpcProtocol<RpcResponse> heartbeatProtocol = new RpcProtocol<>(header, heartbeatResp);
        ctx.writeAndFlush(heartbeatProtocol);
        log.info("[心跳] 服务器收到心跳包，并返回 PONG");
    }

    /**
     * 处理业务请求
     */
    private void handleRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        RpcRequest request = protocol.getContent();
        if (request == null || request.getServiceName() == null) {
            log.warn("[RPC 服务器] 收到空请求，直接丢弃");
            return;
        }
        RpcResponse response = new RpcResponse();
        Header header = protocol.getHeader();
        header.setMsgType(ReqType.RESPONSE.code());

        try {
            // **1. 获取服务**
            Object service = serviceRegistry.getService(request.getServiceName());
            if (service == null) {
                throw new RuntimeException("服务不存在: " + request.getServiceName());
            }

            // **2. 反射调用目标方法**
            Method method = service.getClass().getMethod(
                    request.getMethodName(),
                    getClassTypes(request.getParameterTypes())
            );
            Object result = method.invoke(service, request.getParams());

            // **3. 处理同步/异步返回**
            if (result instanceof CompletableFuture<?>) {
                handleAsyncResponse(ctx, protocol, (CompletableFuture<?>) result);
            } else {
                response.setData(result);
                RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>(header, response);
                ctx.writeAndFlush(responseProtocol);
            }
        } catch (Exception e) {
            handleException(ctx, protocol, e);
        }
    }

    /**
     * 处理异步 RPC 调用
     */
    private void handleAsyncResponse(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol, CompletableFuture<?> future) {
        Header header = protocol.getHeader();
        header.setMsgType(ReqType.RESPONSE.code());

        future.whenComplete((result, ex) -> {
            RpcResponse response = new RpcResponse();
            if (ex != null) {
                response.setMsg("远程调用失败：" + ex.getMessage());
            } else {
                response.setData(result);
            }
            RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>(header, response);
            ctx.writeAndFlush(responseProtocol);
        });
    }

    /**
     * 处理异常
     */
    private void handleException(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol, Exception e) {
        log.error("[RPC 处理失败] 请求: {}, 错误: {}", protocol.getContent(), e.getMessage());

        RpcResponse response = new RpcResponse();
        response.setMsg("远程调用失败：" + e.getMessage());

        Header header = protocol.getHeader();
        header.setMsgType(ReqType.RESPONSE.code());

        RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>(header, response);
        ctx.writeAndFlush(responseProtocol);
    }

    /**
     * 解析参数类型
     */
    private Class<?>[] getClassTypes(String[] parameterTypes) throws ClassNotFoundException {
        Class<?>[] classes = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            classes[i] = Class.forName(parameterTypes[i]);
        }
        return classes;
    }

}
