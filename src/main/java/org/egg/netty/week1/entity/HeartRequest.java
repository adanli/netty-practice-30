package org.egg.netty.week1.entity;

public class HeartRequest extends CustomMessage{
    @Override
    public byte messageType() {
        return 0X01;
    }
}
