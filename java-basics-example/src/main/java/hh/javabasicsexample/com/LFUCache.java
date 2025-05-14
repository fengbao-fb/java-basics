package hh.javabasicsexample.com;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author：HuangHao
 * @Package：hh.javabasicsexample.com
 * @Project：java-basics
 * @Name：LFUCache
 * @Date：2025/5/14 9:19
 * @Des：基于LFU(Least Frequently Used)策略的缓存实现、使用哈希表和分段频率链表实现高效的缓存淘汰机制
 */
public class LFUCache<K, V> {
    // 缓存容量上限
    private final int capacity;
    // 主缓存：键到节点的映射
    private final Map<K, Node<K, V>> cache;
    // 频率映射：频率到对应双向链表的映射
    private final Map<Integer, ConcurrentLinkedQueue<Node<K, V>>> frequencyMap;
    // 当前最小访问频率
    private int minFrequency;
    // 用于保证线程安全的可重入锁
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 构造函数，初始化LFU缓存
     * @param capacity 缓存容量
     */
    public LFUCache(int capacity) {
        this.capacity = capacity;
        // 使用ConcurrentHashMap保证线程安全
        this.cache = new ConcurrentHashMap<>(capacity);
        // 频率映射表，每个频率对应一个队列
        this.frequencyMap = new ConcurrentHashMap<>();
        // 初始最小频率为0
        this.minFrequency = 0;
    }

    /**
     * 从缓存中获取元素
     * @param key 键
     * @return 值，如果不存在则返回null
     */
    public V get(K key) {
        // 快速查找键对应的节点
        Node<K, V> node = cache.get(key);
        // 节点不存在，直接返回null
        if (node == null) {
            return null;
        }
        // 获取锁，保证线程安全
        lock.lock();
        try {
            // 更新节点的访问频率
            updateFrequency(node);
            // 返回节点的值
            return node.value;
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    /**
     * 向缓存中添加或更新元素
     * @param key 键
     * @param value 值
     */
    public void put(K key, V value) {
        // 容量不合法，直接返回
        if (capacity <= 0) {
            return;
        }
        // 获取锁保证线程安全
        lock.lock();
        try {
            // 检查键是否已存在
            Node<K, V> node = cache.get(key);
            if (node != null) {
                // 键已存在，更新值并增加频率
                node.value = value;
                updateFrequency(node);
                return;
            }
            // 键不存在，需要插入
            // 容量已满的话需要淘汰一个LFU节点
            if (cache.size() >= capacity) {
                evict();
            }
            // 创建新节点
            Node<K, V> newNode = new Node<>(key, value);
            // 添加到主缓存
            cache.put(key, newNode);
            // 新节点的频率为1，添加到频率为1的队列
            ConcurrentLinkedQueue<Node<K, V>> queue = frequencyMap.computeIfAbsent(1, k -> new ConcurrentLinkedQueue<>());
            queue.offer(newNode);
            // 插入新节点后最小频率必然是1
            minFrequency = 1;
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    /**
     * 更新节点的访问频率
     * @param node 需要更新的节点
     */
    private void updateFrequency(Node<K, V> node) {
        // 获取当前节点的频率
        int frequency = node.frequency;
        // 获取当前频率对应的队列
        ConcurrentLinkedQueue<Node<K, V>> oldQueue = frequencyMap.get(frequency);
        // 从旧队列中移除该节点
        oldQueue.remove(node);
        // 如果旧队列空了，移除这个频率
        if (oldQueue.isEmpty()) {
            frequencyMap.remove(frequency);
            // 如果这个频率恰好是最小频率，更新最小频率
            if (minFrequency == frequency) {
                minFrequency++;
            }
        }
        // 增加节点的访问频率
        node.frequency++;
        // 获取新频率对应的队列，如果不存在则创建
        ConcurrentLinkedQueue<Node<K, V>> newQueue = frequencyMap.computeIfAbsent(node.frequency, k -> new ConcurrentLinkedQueue<>());
        // 将节点加入新频率对应的队列尾部
        newQueue.offer(node);
    }

    /**
     * 淘汰最不经常使用的元素
     */
    private void evict() {
        // 获取最小频率对应的队列
        ConcurrentLinkedQueue<Node<K, V>> queue = frequencyMap.get(minFrequency);
        // 队列头部的节点是最久未使用的，移除它
        Node<K, V> lfuNode = queue.poll();
        // 如果队列空了，移除这个频率
        if (queue.isEmpty()) {
            frequencyMap.remove(minFrequency);
        }
        // 从主缓存中移除该节点
        cache.remove(lfuNode.key);
    }

    /**
     * 缓存节点类，存储键值对和访问频率
     */
    private static class Node<K, V> {
        // 键
        K key;
        // 值
        V value;
        // 访问频率
        int frequency;

        /**
         * 节点构造函数
         * @param key 键
         * @param value 值
         */
        Node(K key, V value) {
            this.key = key;
            this.value = value;
            // 新节点初始频率为1
            this.frequency = 1;
        }
    }

    public static void main(String[] args) {
        LFUCache lfuCache = new LFUCache(2);
        lfuCache.put("a", "a");
        lfuCache.put("b", "b");
        System.out.println(lfuCache.get("a")); // a
        lfuCache.put("c", "c");
        System.out.println(lfuCache.get("b"));// null
        System.out.println(lfuCache.get("c"));//c
        lfuCache.put("d", "d");
        System.out.println(lfuCache.get("a"));//null
        System.out.println(lfuCache.get("c"));//c
        System.out.println(lfuCache.get("d"));//d
        lfuCache.put("a", "a");
        System.out.println(lfuCache.get("a"));
        System.out.println(lfuCache.get("b"));
        System.out.println(lfuCache.get("c"));
        System.out.println(lfuCache.get("d"));
    }
}
