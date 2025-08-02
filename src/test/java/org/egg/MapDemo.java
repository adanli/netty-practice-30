package org.egg;

import java.util.HashMap;
import java.util.Map;

public class MapDemo {
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");

        String u = map.putIfAbsent("5", "5");
        System.out.println(u);

        String v = map.putIfAbsent("5", "7");
        System.out.println(v);
    }
}
