package org.egg;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class QueueTest extends TestCase {
    public void testQueueOperator() throws Exception{
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

        System.out.println("=====put=====");
        queue.put("1");
        queue.put("2");
        queue.put("3");
        queue.put("4");
        queue.put("5");

        print(queue);

        System.out.println("=====poll=====");
        String s1 = queue.poll();
        System.out.println("=====poll:"+s1+"=====");
        String s2 = queue.poll();
        System.out.println("=====poll:"+s2+"=====");
        String s3 = queue.poll();
        System.out.println("=====poll:"+s3+"=====");
        print(queue);

        queue.put("1");
        queue.put("2");
        print(queue);

        System.out.println("=====take=====");
        String s4 = queue.take();
        System.out.println("=====take:"+s4+"=====");
        String s5 = queue.take();
        System.out.println("=====take:"+s5+"=====");
        String s6 = queue.take();
        System.out.println("=====take:"+s6+"=====");
        print(queue);

        System.out.println("=====offer=====");
        queue.offer("6");
        queue.offer("7");
        queue.offer("8");
        print(queue);

//        System.out.println("=====add=====");
//        queue.add("9");
//        queue.add("10");
//        queue.add("11");
        System.out.println("=====put=====");
        queue.put("9");
        System.out.println("=====put:9=====");
        queue.put("10");
        System.out.println("=====put:10=====");
        queue.put("11");
        System.out.println("=====put:11=====");
        print(queue);

    }

    private void print(BlockingQueue<String> queue) {
        Iterator<String> iterator = queue.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            String s = iterator.next();
            sb.append(s).append(",");
        }
        if(!sb.isEmpty()) {
            System.out.println(sb.substring(0, sb.length()-1));
        }
    }

}
