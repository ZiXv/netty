package com.rpc.serialization;

import com.rpc.protocol.constant.SerialType;

import java.io.*;

public class JavaSerializer implements  ISerializer{
    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("待序列化对象不能为空");
        }
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(obj);
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("序列化对象失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("待反序列化字节数组不能为空");
        }
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInputStream in = new ObjectInputStream(byteIn)) {
            Object obj = in.readObject();
            return clazz.cast(obj);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("反序列化对象失败", e);
        }
    }


    @Override
    public byte getType() {
        return SerialType.JAVA_SERIAL.code();
    }
}
