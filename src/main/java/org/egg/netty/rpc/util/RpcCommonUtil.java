package org.egg.netty.rpc.util;

public class RpcCommonUtil {
    public static final int MAGIC_NUMBER = 0XCAFEBABE;
    public static final byte VERSION = 0X01;
    public static final byte RPC_REQUEST = 0X01;
    public static final byte RPC_RESPONSE = 0X02;
    public static final byte HEARTBEAT_REQUEST = 0X03;
    public static final byte HEARTBEAT_RESPONSE = 0X04;
    public static final short REMAIN_BYTE = 0X0000;

}
