package org.egg.netty.week1.entity;

public class HeartResponse extends CustomMessage{
    @Override
    public byte messageType() {
        return 0X02;
    }
}
