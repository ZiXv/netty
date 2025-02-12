package com.rpc.protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class RpcRequest implements Serializable {
    private String serviceName;
    private String methodName;
    //调用方在构造 RpcRequest 时，应保证 params 与 parameterTypeNames 数组的长度和顺序是一致的。
    //反序列化后，服务端可以通过 Class.forName(parameterTypeName) 得到对应的 Class 对象，再通过反射调用方法。
    private Object[] params;
    private String[] parameterTypes;
    private Map<String, Object> attachments;
}
