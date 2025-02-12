package com.rpc.protocol.constant;

public enum ReqType {

    REQUEST((byte)1),
    RESPONSE((byte)2),
    HEARTBEAT_REQUEST((byte)3),
    HEARTBEAT_RESPONSE((byte)4);



    private byte code;

    private ReqType(byte code) {
        this.code=code;
    }

    public byte code(){
        return this.code;
    }
    public static ReqType findByCode(int code) {
        for (ReqType msgType : ReqType.values()) {
            if (msgType.code() == code) {
                return msgType;
            }
        }
        return null;
    }
}
