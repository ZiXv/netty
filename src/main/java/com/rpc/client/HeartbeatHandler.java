package com.rpc.client;

import com.rpc.protocol.Header;
import com.rpc.protocol.RpcProtocol;
import com.rpc.protocol.RpcRequest;
import com.rpc.protocol.RpcResponse;
import com.rpc.protocol.constant.ReqType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import static com.rpc.protocol.constant.RpcConstant.MAGIC;
import static com.rpc.protocol.constant.RpcConstant.VERSION;

/**
 * 处理心跳包，只在 30s 内无数据时发送心跳
 */
@Slf4j
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("心跳检测30s 无数据，发送心跳包...");
                Header header = new Header();
                header.setMagic(MAGIC);
                header.setVersion(VERSION);
                header.setMsgType(ReqType.HEARTBEAT_REQUEST.code());
                RpcRequest heartbeatRequest = new RpcRequest();
                RpcProtocol<RpcRequest> heartbeatProtocol = new RpcProtocol<>(header, heartbeatRequest);
                ctx.writeAndFlush(heartbeatProtocol);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcProtocol<?>) {
            RpcProtocol<?> protocol = (RpcProtocol<?>) msg;
            Header header = protocol.getHeader();

            if (header.getMsgType() == ReqType.HEARTBEAT_RESPONSE.code()) {
                RpcResponse heartbeatResp = new RpcResponse();
                heartbeatResp.setData("PONG");
                RpcProtocol<RpcResponse> heartbeatProtocol = new RpcProtocol<>(header, heartbeatResp);
                log.info("[客户端心跳] 收到服务器 PONG 响应");
                ctx.writeAndFlush(heartbeatProtocol);
                return;
            }
        }
        super.channelRead(ctx, msg);  // **非心跳包，正常传递**
    }
}
