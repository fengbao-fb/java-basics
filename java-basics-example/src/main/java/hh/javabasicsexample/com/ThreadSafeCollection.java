package hh.javabasicsexample.com;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author HuangHao
 * @since 2025/5/14 14:00
 * @des 线程安全集合
 */
public class ThreadSafeCollection {

    public static void main(String[] args) throws Exception{
        //1. Vector
        Vector<String> vectorList = new Vector<>();
        vectorList.add("item1");
        System.out.println(vectorList.get(0));
        //2. Hashtable
        Hashtable<String, String> hashTableMap = new Hashtable<>();
        hashTableMap.put("1", "A");
        System.out.println(hashTableMap.get("1"));
        //3. ConcurrentHashMap
        ConcurrentHashMap<String, Integer> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put("count", 0);
        System.out.println(concurrentHashMap.compute("count", (k, v) -> v + 1));
        //4. CopyOnWriteArrayList/CopyOnWriteSet
        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        copyOnWriteArrayList.add("element");
        //迭代期间安全
        for (String s : copyOnWriteArrayList) {
            System.out.println(s);
        }
        CopyOnWriteArraySet<String> copyOnWriteArraySet = new CopyOnWriteArraySet<>();
        copyOnWriteArraySet.add("element");
        //5. BlockingQueue
        ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(10);
        //阻塞写入
        arrayBlockingQueue.put("task");
        //阻塞获取
        String take = arrayBlockingQueue.take();
        //6. ConcurrentLinkedQueue
        ConcurrentLinkedQueue<String> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        concurrentLinkedQueue.offer("event");
        String event = concurrentLinkedQueue.poll();
    }
}
