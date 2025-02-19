package com.rpc.protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class RpcResponse implements Serializable {

    private Object data;
    private String msg;
    private Map<String, Object> attachments;

}
