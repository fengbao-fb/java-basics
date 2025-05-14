package hh.javabasicsexample.com;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author：HuangHao
 * @Package：hh.javabasicsexample.com
 * @Project：java-basics
 * @Name：LRUCache
 * @Date：2025/5/13 15:42
 * @Des：支持并发访问的LRU缓存实现、使用ConcurrentHashMap存储缓存项、使用双向链表维护访问顺序、使用分段锁提高并发性能
 */
public class LRUCache {
    /**
     * 双向链表节点类，存储缓存的键值对
     * 每个节点包含前驱和后继引用，形成双向链表结构
     */
    private static class DLinkedNode {
        String key;           // 缓存键
        String value;         // 缓存值
        DLinkedNode prev;  // 前驱节点引用
        DLinkedNode next;  // 后继节点引用

        public DLinkedNode() {}

        public DLinkedNode(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    // 使用ConcurrentHashMap存储缓存项，提供线程安全的基本操作
    private final ConcurrentMap<String, DLinkedNode> cache = new ConcurrentHashMap<>();
    private int size;                // 当前缓存大小
    private final int capacity;      // 缓存容量上限
    private final DLinkedNode head;  // 双向链表头节点（虚拟节点）
    private final DLinkedNode tail;  // 双向链表尾节点（虚拟节点）

    // 分段锁实现 - 使用多个锁减少竞争
    private static final int SEGMENT_COUNT = 16;  // 锁分段数量
    private final ReentrantLock[] locks;          // 分段锁数组

    /**
     * 构造函数，初始化缓存
     * @param capacity 缓存容量上限
     */
    public LRUCache(int capacity) {
        this.size = 0;
        this.capacity = capacity;

        // 初始化双向链表，头节点和尾节点为虚拟节点
        head = new DLinkedNode();
        tail = new DLinkedNode();
        head.next = tail;
        tail.prev = head;

        // 初始化分段锁数组
        locks = new ReentrantLock[SEGMENT_COUNT];
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    /**
     * 根据key获取对应的锁
     * 通过哈希算法将key映射到固定的锁
     * @param key 缓存键
     * @return 对应的锁实例
     */
    private ReentrantLock getLockFor(String key) {
        return locks[(key.hashCode() & 0x7FFFFFFF) % SEGMENT_COUNT];
    }

    /**
     * 获取缓存值
     * @param key 缓存键
     * @return 存在则返回对应值，不存在返回-1
     */
    public String get(String key) {
        // 先检查缓存中是否存在该key
        DLinkedNode node = cache.get(key);
        if (node == null) {
            return null;
        }

        // 获取该key对应的锁，保证操作的原子性
        ReentrantLock lock = getLockFor(key);
        //加锁
        lock.lock();
        try {
            // 双重检查，防止在等待锁的过程中节点被其他线程移除
            node = cache.get(key);
            if (node != null) {
                // 将访问的节点移动到链表头部，表示最近使用
                moveToHead(node);
            }
        } finally {
            //释放锁
            lock.unlock();
        }
        return node != null ? node.value : null;
    }

    /**
     * 添加或更新缓存项
     * @param key 缓存键
     * @param value 缓存值
     */
    public void put(String key, String value) {
        // 获取该key对应的锁
        ReentrantLock lock = getLockFor(key);
        lock.lock();
        try {
            // 检查缓存中是否已存在该key
            DLinkedNode node = cache.get(key);
            if (node == null) {
                // 不存在则创建新节点
                DLinkedNode newNode = new DLinkedNode(key, value);
                // 添加到哈希表中
                cache.put(key, newNode);
                // 添加到链表头部
                addToHead(newNode);
                // 增加缓存大小
                incrementSize();

                // 检查是否超过容量上限，超过则淘汰最老的节点
                if (size > capacity) {
                    evict();
                }
            } else {
                // 已存在则更新值
                node.value = value;
                // 移动到链表头部，表示最近使用
                moveToHead(node);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 增加缓存大小（线程安全）
     */
    private void incrementSize() {
        // 使用第一个锁作为全局锁，保证size变量的原子性操作
        ReentrantLock globalLock = locks[0];
        globalLock.lock();
        try {
            size++;
        } finally {
            globalLock.unlock();
        }
    }

    /**
     * 减少缓存大小（线程安全）
     */
    private void decrementSize() {
        // 使用第一个锁作为全局锁
        ReentrantLock globalLock = locks[0];
        globalLock.lock();
        try {
            size--;
        } finally {
            globalLock.unlock();
        }
    }

    /**
     * 淘汰最老的缓存项（线程安全）
     */
    private void evict() {
        // 使用全局锁，确保只有一个线程执行淘汰操作
        ReentrantLock globalLock = locks[0];
        globalLock.lock();
        try {
            // 再次检查容量，防止其他线程已经执行过驱逐
            if (size <= capacity) {
                return;
            }

            // 获取链表尾部节点（最老的节点）
            DLinkedNode tailNode = tail.prev;
            if (tailNode != head) {
                // 从链表中移除该节点
                removeNode(tailNode);
                // 从哈希表中移除
                cache.remove(tailNode.key);
                // 减少缓存大小
                decrementSize();
            }
        } finally {
            globalLock.unlock();
        }
    }

    /**
     * 将节点添加到链表头部
     * @param node 待添加的节点
     */
    private void addToHead(DLinkedNode node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * 从链表中移除节点
     * @param node 待移除的节点
     */
    private void removeNode(DLinkedNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /**
     * 将节点移动到链表头部
     * @param node 待移动的节点
     */
    private void moveToHead(DLinkedNode node) {
        removeNode(node);
        addToHead(node);
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) {
        LRUCache cache = new LRUCache(2);
        cache.put("a", "a");
        cache.put("b", "b");
        System.out.println(cache.get("a")); // 返回 a
        cache.put("c", "c"); // 该操作会使得关键字 b 作废
        System.out.println(cache.get("b")); // 返回 null (未找到)
        cache.put("d", "d"); // 该操作会使得关键字 c 作废
        System.out.println(cache.get("a")); // 返回 null (未找到)
        System.out.println(cache.get("c")); // 返回 c
        System.out.println(cache.get("d")); // 返回 d
    }
}