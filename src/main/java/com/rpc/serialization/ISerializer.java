package com.rpc.serialization;

public interface ISerializer {
    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
    byte getType();

}
