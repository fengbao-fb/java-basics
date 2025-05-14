package hh.javabasicsexample.com;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * @author HuangHao
 * @since 2025/5/14 14:00
 * @des 如何优雅删除HashMap的元素
 */
public class HashMapDelDemo {

    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<String, String>() {{
            put("1", "1");
            put("2", "2");
            put("3", "3");
            put("4", "4");
            put("5", "5");
        }};
        //1. 增强for
        CopyOnWriteArraySet<Map.Entry<String, String>> entries = new CopyOnWriteArraySet<>(map.entrySet());
        for (Map.Entry<String, String> entry : entries) {
            if (StringUtils.equals(entry.getKey(), "1")){
                map.remove(entry.getKey());
            }
        }
        System.out.println(map);

        //2. foreach删除
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>(map);
        concurrentHashMap.forEach((k, v) -> {
            if (StringUtils.equals(k, "2")){
                map.remove(k);
            }
        });
        System.out.println(map);

        //3. 使用Iterator迭代器删除
        ConcurrentHashMap<String, String> iteratorConcurrentHashMap = new ConcurrentHashMap<>();
        Iterator<Map.Entry<String, String>> iterator = new ConcurrentHashMap<>(map).entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> next = iterator.next();
            if (StringUtils.equals(next.getKey(), "3")){
                iterator.remove();
            }else {
                iteratorConcurrentHashMap.put(next.getKey(), next.getValue());
            }
        }
        System.out.println(iteratorConcurrentHashMap);

        //4. 使用removeIf
        ConcurrentHashMap<String, String> removeIfConHashMap = new ConcurrentHashMap<>(map);
        removeIfConHashMap.entrySet().removeIf(entry -> StringUtils.equals(entry.getKey(), "4"));
        System.out.println(map);

        //5. stream流删除
        ConcurrentHashMap<String, String> streamConHashMap = new ConcurrentHashMap<>(map);
        ConcurrentMap<String, String> streamMap = streamConHashMap.entrySet().stream().filter(entry -> StringUtils.equals(entry.getKey(), "5")).collect(Collectors.toConcurrentMap(ConcurrentHashMap.Entry::getKey, ConcurrentHashMap.Entry::getValue));
        System.out.println(streamMap);
    }
}
