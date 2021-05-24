# LinkedHashMap

[TOC]

## 简介

趁着热乎劲，赶紧看下`LinkedHashMap`，直观上来看，它依旧是一个`HashMap`，但是又是`Linked`,有序的链表,其实就是把节点穿插成双向链表

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