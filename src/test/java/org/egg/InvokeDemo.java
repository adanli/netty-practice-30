package org.egg;

import java.lang.reflect.Method;

public class InvokeDemo {
    public static void main(String[] args) throws Exception{
        Class<?> clazz = Class.forName("org.egg.demo.PrintDemo");
        Object instance = clazz.getConstructor().newInstance();
        String method = "hello";
        Object[] arguments = new Object[0];

        Method m = clazz.getMethod(method);
        Object result = m.invoke(instance, arguments);
        System.out.println(result);

    }
}
