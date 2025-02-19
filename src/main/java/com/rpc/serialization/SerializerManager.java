package com.rpc.serialization;

import com.rpc.protocol.constant.SerialType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class SerializerManager {

    private static final Map<Byte, ISerializer> serializerMap = new ConcurrentHashMap<>();

    static {
        // 注册所有实现 ISerializer 接口的序列化器
        serializerMap.put(SerialType.JSON_SERIAL.code(), new JsonSerializer());
        serializerMap.put(SerialType.JAVA_SERIAL.code(), new JavaSerializer());
        serializerMap.put(SerialType.PROTOBUF_SERIAL.code(), new ProtoBufSerializer());
    }

    /**
     * 根据传入的序列化类型获取相应的 ISerializer 实例。
     *
     * @param type 序列化类型标识，例如从 RPC 消息头中获取的 serialType 字段
     * @return 对应的 ISerializer 实现
     * @throws RuntimeException 如果没有找到对应的序列化器
     */
    public static ISerializer getSerializer(byte type) {
        ISerializer serializer = serializerMap.get(type);
        if (serializer == null) {
            throw new RuntimeException("不支持的序列化类型: " + type);
        }
        return serializer;
    }
}
