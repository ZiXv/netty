package com.rpc.protocol;

import com.rpc.protocol.constant.ReqType;
import com.rpc.protocol.constant.RpcConstant;
import com.rpc.serialization.ISerializer;
import com.rpc.serialization.SerializerManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.rpc.protocol.constant.RpcConstant.HEAD_TOTAL_LEN;

public class RpcDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 如果可读字节不足 Header 长度，直接返回，等待后续数据
        if (in.readableBytes() < HEAD_TOTAL_LEN) {
            return;
        }
        in.markReaderIndex();

        // 按顺序读取 Header 字段
        short magic = in.readShort();          // 2 字节：魔数
        if(magic!= RpcConstant.MAGIC){
            throw new IllegalArgumentException("Illegal request parameter 'magic',"+magic);
        }
        byte version = in.readByte();            // 1 字节：版本
        byte serialType = in.readByte();         // 1 字节：序列化类型
        byte msgType = in.readByte();            // 1 字节：消息类型
        long msgId = in.readLong();              // 8 字节：请求 ID
        int length = in.readInt();               // 4 字节：消息体长度

        // 如果当前 ByteBuf 中可读字节小于消息体长度，则重置读指针等待更多数据
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        // 读取消息体数据
        byte[] data = new byte[length];
        in.readBytes(data);

        Header header = new Header(magic, version, serialType, msgType, msgId, length);

        // 根据序列化类型选择对应的序列化器
        ISerializer serializer = SerializerManager.getSerializer(serialType);
        ReqType rt=ReqType.findByCode(msgType);

        // 根据消息类型反序列化消息体，这里假设：
        // - MessageType.REQUEST 为请求消息，对应 RpcRequest 类；
        // - MessageType.RESPONSE 为响应消息，对应 RpcResponse 类；
        // - 心跳消息通常没有消息体，可直接设置为 null
        switch(rt){
            case REQUEST:
                RpcRequest request=serializer.deserialize(data, RpcRequest.class);
                RpcProtocol<RpcRequest> reqProtocol=new RpcProtocol<>();
                reqProtocol.setHeader(header);
                reqProtocol.setContent(request);
                out.add(reqProtocol);
                break;
            case RESPONSE:
                RpcResponse response=serializer.deserialize(data,RpcResponse.class);
                RpcProtocol<RpcResponse> resProtocol=new RpcProtocol<>();
                resProtocol.setHeader(header);
                resProtocol.setContent(response);
                out.add(resProtocol);
                break;
            case HEARTBEAT_REQUEST:
                RpcProtocol<Void> hbReqProtocol = new RpcProtocol<>();
                hbReqProtocol.setHeader(header);
                hbReqProtocol.setContent(null);  // 或者可以设置一个标识心跳请求的常量对象
                out.add(hbReqProtocol);
                break;
            case HEARTBEAT_RESPONSE:
                RpcProtocol<Void> hbResProtocol = new RpcProtocol<>();
                hbResProtocol.setHeader(header);
                hbResProtocol.setContent(null);  // 或者可以设置一个标识心跳响应的常量对象
                out.add(hbResProtocol);
                break;
            default:

                break;
        }
    }
}
