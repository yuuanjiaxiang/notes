# ConcurrentHashMap 

[TOC]

#### 1.继承关系

继承自 `AbstractMap` ,实现了 `Map` 接口

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable{}
```

#### 2.几个变量

```java
// 默认容量，容量必须是2的幂
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

// 最大容量
static final int MAXIMUM_CAPACITY = 1 << 30;

// 加载因子，当容量>Capacity*load_factor时进行扩容
static final float DEFAULT_LOAD_FACTOR = 0.75f;

// 桶转换为树的阈值
static final int TREEIFY_THRESHOLD = 8;

// 转换为桶的阈值
static final int UNTREEIFY_THRESHOLD = 6;

// 树化容量阈值。为了避免进行扩容、树形化选择的冲突，这个值不能小于 4 * TREEIFY_THRESHOLD
static final int MIN_TREEIFY_CAPACITY = 64;
```

#### 3.put

像 `afterNode***` 这种方法都是为LinkedHashMap预留的，`HashMap`里的实现都是空的

```java
/**
 * Implements Map.put and related methods.
 *
 * @param hash hash for key
 * @param key the key
 * @param value the value to put
 * @param onlyIfAbsent  为true 不改变现有值
 * @param evict flase 是创建模式
 * @return previous value, or null if none
 */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // 如果数组为空，就初始化一下，n是数组长度
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    // 如果key hash值的链表头结点是空，那么新建一个
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        // 如果这是个链表并且key不为空，且头结点p等于传入的key值
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        // 否则 p 是个红黑树，使用红黑树putval
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            // 到这里说明是链表但不是头结点，那就循环找key相等的节点，binCount记录的是链表长度
            for (int binCount = 0; ; ++binCount) {
                // 找到结尾都还没找到，那就新加一个节点
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    // 链表长度再长就要到阈值了，得转换成树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                // 跟e = p.next配合，链表向后遍历
                p = e;
            }
        }
        // 当找到的节点不为空是，看是否需要替换当前值
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    // 操作数+1，据此判断是否有并发操作风险
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

其中值得注意的是`treeifyBin()`方法，将链表转换为红黑树，需要再详细了解 `TreeNode.putTreeVal()`看一下实现

#### 4.get

 get方法比较简单，只要找不到返回null就完事了，而且不涉及数据结构的变更

```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}

/**
 * Implements Map.get and related methods.
 *
 * @param hash hash for key
 * @param key the key
 * @return the node, or null if none
 */
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    // 当时数组不为空，且长度>0s时候，并且hash取模有元素
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        //要是第一个直接返回第一个节点
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        // 有后续节点不为空
        if ((e = first.next) != null) {
            // 要是第一个节点是树节点，就从树节点里取
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            // 链表的话就循环遍历，没有那只能返回null了
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

