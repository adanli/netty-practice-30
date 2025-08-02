package org.egg.netty.codec.multiserializer.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JavaSerializer {
    public static byte[] serialize(Object payload) throws Exception{
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(payload);
            objectOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        }

    }

    public static Object deserialize(byte[] bytes) throws Exception {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))){
            return objectInputStream.readObject();
        }
    }
}
