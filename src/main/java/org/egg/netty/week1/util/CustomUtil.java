package org.egg.netty.week1.util;

public class CustomUtil {
    public static final int MAGIC_NUMBER = 0X12345678;
    public static final byte HEART_REQUEST = 0X01;
    public static final byte HEART_RESPONSE = 0X02;
    public static final byte BUSINESS_REQUEST = 0X10;
    public static final byte BUSINESS_RESPONSE = 0X20;
    public static final int MAX_FRAME_LENGTH = 1024;
    public static final int PORT = 8088;
}
