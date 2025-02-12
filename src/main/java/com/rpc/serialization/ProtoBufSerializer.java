package com.rpc.serialization;

import com.google.protobuf.Message;
import com.rpc.protocol.constant.SerialType;

import java.lang.reflect.Method;

public class ProtoBufSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("待序列化对象不能为空");
        }
        if (!(obj instanceof Message)) {
            throw new IllegalArgumentException("待序列化对象必须是 Protobuf 消息类型，即实现 com.google.protobuf.Message");
        }
        // 直接调用 Protobuf 对象的 toByteArray() 方法
        return ((Message) obj).toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("待反序列化字节数组不能为空");
        }
        try {
            // 所有 Protobuf 生成的类均包含静态方法 parseFrom(byte[] data)
            Method parseFromMethod = clazz.getDeclaredMethod("parseFrom", byte[].class);
            Object message = parseFromMethod.invoke(null, bytes);
            return clazz.cast(message);
        } catch (Exception e) {
            throw new RuntimeException("反序列化对象失败", e);
        }
    }

    @Override
    public byte getType() {
        return SerialType.PROTOBUF_SERIAL.code();
    }
}
