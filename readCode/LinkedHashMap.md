# LinkedHashMap

[TOC]

## 简介

趁着热乎劲，赶紧看下`LinkedHashMap`，直观上来看，它依旧是一个`HashMap`，但是又是`Linked`,有序的链表,其实就是把节点穿插成双向链表，由于继承的`hashMap`，所以它各种方法其实一样的，只不过在做完map的数组+链表/红黑树部分后，要把链表也做相应的操作，之前也看到过，具体方法为`afterNodeRemoval`,`afterNodeInsertion`,`afterNodeAccess`

![LinkedHashMap](image\LinkedHashMap.png)



双向链表，每个元素需要记录前置节点跟后置节点，整个Map也需要记录头结点跟尾节点，比较直观

```java
/**
 * HashMap.Node subclass for normal LinkedHashMap entries.
 */
static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before, after;
    Entry(int hash, K key, V value, Node<K,V> next) {
        super(hash, key, value, next);
    }
}

/**
 * The head (eldest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> head;

/**
 * The tail (youngest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> tail;

/**
* The iteration ordering method for this linked hash map: <tt>true</tt>
* for access-order, <tt>false</tt> for insertion-order.
*
* @serial
*/
final boolean accessOrder;// true为访问顺序，false插入顺序，默认false
```

## afterNodeRemoval

移除节点后，把node从链表中删除，要考虑前后节点为空的问题

```java
void afterNodeRemoval(Node<K,V> e) { // unlink
    LinkedHashMap.Entry<K,V> p =
        (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
    p.before = p.after = null;
    if (b == null)
        head = a;
    else
        b.after = a;
    if (a == null)
        tail = b;
    else
        a.before = b;
}
```

## afterNodeInsertion

插入节点后操作，实际上removeEldestEntry总是返回false,可以通过覆写该方法实现**LRU缓存**

```java
void afterNodeInsertion(boolean evict) { // possibly remove eldest
    LinkedHashMap.Entry<K,V> first;
    if (evict && (first = head) != null && removeEldestEntry(first)) {
        K key = first.key;
        removeNode(hash(key), key, null, false, true);
    }
}
```

## afterNodeAccess

```java
void afterNodeAccess(Node<K,V> e) { // move node to last
    LinkedHashMap.Entry<K,V> last;
    // 如果是按照访问顺序，那么把节点e交换到尾节点
    if (accessOrder && (last = tail) != e) {
        LinkedHashMap.Entry<K,V> p =
            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.after = null;
        if (b == null)
            head = a;
        else
            b.after = a;
        if (a != null)
            a.before = b;
        else
            last = b;
        if (last == null)
            head = p;
        else {
            p.before = last;
            last.after = p;
        }
        tail = p;
        ++modCount;
    }
}
```

## 使用LinkedHashMap实现LRU缓存

通过override removeEldestEntry()方法，实现了一个大小为cacheCapacity的LRU缓存类

```java
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> implements Map<K, V> {

    private static final long serialVersionUID = 1L;

    private int cacheCapacity;

    public LRUCache(int cacheCapacity, int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
        this.cacheCapacity = cacheCapacity;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        // TODO Auto-generated method stub
        return size() > cacheCapacity;
    }
    
}
```