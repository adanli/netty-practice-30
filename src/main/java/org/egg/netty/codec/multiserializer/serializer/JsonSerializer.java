package org.egg.netty.codec.multiserializer.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer {
    public static byte[] serialize(Object payload) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(payload);
    }

    public static Object deserialize(byte[] bytes) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(bytes, Object.class);
    }
}
