package com.rpc.serialization;
import com.alibaba.fastjson.JSON;
import com.rpc.protocol.constant.SerialType;
import java.nio.charset.StandardCharsets;

public class JsonSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("待序列化对象不能为空");
        }
        String jsonString = JSON.toJSONString(obj);
        return jsonString.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("待反序列化数据不能为空");
        }
        String jsonString = new String(data, StandardCharsets.UTF_8);
        return JSON.parseObject(jsonString, clazz);
    }

    @Override
    public byte getType() {
        return SerialType.JSON_SERIAL.code();
    }
}