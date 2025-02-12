package com.rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class Header implements Serializable {
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
 **/
    private short magic;
    private byte version;
    private byte serialType;
    private byte msgType;
    private long msgId;
    private int length;

}
