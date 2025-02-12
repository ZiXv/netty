package com.rpc.protocol;
import com.rpc.serialization.ISerializer;
import com.rpc.serialization.SerializerManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcEncoder  extends MessageToByteEncoder<RpcProtocol<Object>> {
    /**
     * ---------------------------------------------------------------------
     * | Field       | Size (Bytes) | Offset Range | Description
     * ---------------------------------------------------------------------
     * | magic       |      2       |    0 - 1     | 魔数 - 用来验证报文的身份
     * | version     |      1       |       2      | 协议版本
     * | serialType  |      1       |       3      | 序列化类型
     * | msgType     |      1       |       4      | 消息类型
     * | msgId       |      8       |    5 - 12    | 请求 ID
     * | length      |      4       |   13 - 16    | 数据长度
     * ---------------------------------------------------------------------
     *     private short magic;
     *     private byte version;
     *     private byte serialType;
     *     private byte msgType;
     *     private long msgId;
     *     private int length;
     **/
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        log.info("=============begin RpcEncoder============");
        Header header=msg.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getSerialType());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeLong(header.getMsgId());
        ISerializer serializer= SerializerManager.getSerializer(header.getSerialType());
        byte[] data=serializer.serialize(msg.getContent()); //序列化
        header.setLength(data.length);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);

    }
}
