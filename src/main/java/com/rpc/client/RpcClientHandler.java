package com.rpc.client;

import com.rpc.protocol.Header;
import com.rpc.protocol.RpcProtocol;
import com.rpc.protocol.RpcRequest;
import com.rpc.protocol.RpcResponse;
import com.rpc.protocol.constant.ReqType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.rpc.protocol.constant.RpcConstant.MAGIC;
import static com.rpc.protocol.constant.RpcConstant.VERSION;

/**
 * Netty 客户端处理器：处理 RPC 响应
 *
 */
@Slf4j
public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    // 存储 requestId -> CompletableFuture<RpcResponse>，保证异步响应
    private final Map<Long, CompletableFuture<RpcResponse>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * 发送 RPC 请求
     */

    public CompletableFuture<RpcResponse> sendRpcRequest(Channel channel, RpcProtocol<RpcRequest> request) {
        UUID uuid = UUID.randomUUID();
        long requestId = uuid.getMostSignificantBits();
        request.getHeader().setMsgId(requestId);
        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        channel.writeAndFlush(request).addListener(f -> {
            if (!f.isSuccess()) {
                pendingRequests.remove(requestId);
                future.completeExceptionally(f.cause());
            }
        });

        future.orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    // 超时或其他异常时，从 pendingRequests 移除
                    pendingRequests.remove(requestId);
                    return null;
                });

        return future;
    }


    /**
     * 处理服务器返回的 RPC 响应
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object Msg) {
        RpcProtocol<RpcResponse> msg = (RpcProtocol<RpcResponse>) Msg;
        byte msgType = msg.getHeader().getMsgType();

        if (msgType == ReqType.HEARTBEAT_RESPONSE.code()) {
            log.info("收到服务端心跳响应: {}", msg.getContent());
            return;
        }
        else {
            long requestId = msg.getHeader().getMsgId();
            CompletableFuture<RpcResponse> future = pendingRequests.remove(requestId);
            if (future != null) {
                future.complete(msg.getContent());
            } else {
                System.err.println("No matching future for requestId: " + requestId);
            }
        }
    }

    /**
     * 发生异常时，通知所有未完成的 Future
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        for (CompletableFuture<RpcResponse> future : pendingRequests.values()) {
            future.completeExceptionally(cause);
        }
        pendingRequests.clear();
        ctx.close();
    }
    public void sendHeartbeat(Channel channel) {
        if (channel == null || !channel.isActive()) {
            // 无法发送心跳
            return;
        }

        // 构造 Header
        Header header = new Header();
        header.setMsgType(ReqType.HEARTBEAT_REQUEST.code());
        header.setMagic(MAGIC);
        header.setVersion(VERSION);
        // 如果你还有其他字段，比如 requestId 等，也可以设置
        header.setMsgId(ThreadLocalRandom.current().nextLong());

        // 内容可以是空的 RpcRequest，也可以自定义
        RpcRequest heartbeatReq = new RpcRequest();

        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>(header, heartbeatReq);
        channel.writeAndFlush(protocol);
    }

}
