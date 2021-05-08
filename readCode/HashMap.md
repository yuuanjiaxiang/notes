# ConcurrentHashMap 

<!-- toc -->

## 1.继承关系

继承自 `AbstractMap` ,实现了 `Map` 接口,但是其实有点奇怪的是，明明它们的方法都是一样的，[在stackoverflow找到了答案（点击这里跳转）](https://stackoverflow.com/questions/2165204/why-does-linkedhashsete-extend-hashsete-and-implement-sete)

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable{}
```

## 2.几个变量

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

## 3.put

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

### 3.1.treeifyBin()

```java
/**
 * Replaces all linked nodes in bin at index for given hash unless
 * table is too small, in which case resizes instead.
 */
final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    // 如果数组为空或者数组没到成树条件，只扩容大小
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        resize();
    // 找到当前链表，如果不为，那就先把头结点转换为TreeNode,然后维护一个头结点hd跟尾节点tl
    else if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);
            if (tl == null)
                hd = p;
            // 其实操作下来就是把整个链表转换为了TreeNode的双向链表
            else {
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        // 到这里我们发现，其实还没转换成红黑树，继续看treeify函数
        if ((tab[index] = hd) != null)
            hd.treeify(tab);
    }
}
```

### 3.2.TreeNode.treeify

复习红黑树之后补充

### 3.3.TreeNode.putTreeVal(

复习红黑树之后补充

## 4.get

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

### 4.1.TreeNode.getTreeNode

红黑树作为一颗平衡二叉树，我们很容易根据hash值的大小来搜索指定的节点，没有则返回null

```java
/**
 * Finds the node starting at root p with the given hash and key.
 * The kc argument caches comparableClassFor(key) upon first use
 * comparing keys.
 */
final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
    TreeNode<K,V> p = this;
    do {
        int ph, dir; K pk;
        TreeNode<K,V> pl = p.left, pr = p.right, q;
        // 小于走左子树，大于走右子树，等于直接返回当前节点
        if ((ph = p.hash) > h)
            p = pl;
        else if (ph < h)
            p = pr;
        // hash相等，并且地址或equals相等，就是这个节点了
        else if ((pk = p.key) == k || (k != null && k.equals(pk)))
            return p;
        
        // hash相等但是不equals,这个时候，当前树为空走另一边
        else if (pl == null)
            p = pr;
        else if (pr == null)
            p = pl;
        
        // 都不为空，则通过compare方法来比较大小，决定走那边子树，继续递归的去比较
        else if ((kc != null ||
                  (kc = comparableClassFor(k)) != null) &&
                 (dir = compareComparables(kc, k, pk)) != 0)
            p = (dir < 0) ? pl : pr;
        else if ((q = pr.find(h, k, kc)) != null)
            return q;
        else
            p = pl;
    } while (p != null);
    // 没找到相应节点就返回null
    return null;
}

/**
 * Calls find for root node.
 */
final TreeNode<K,V> getTreeNode(int h, Object k) {
    return ((parent != null) ? root() : this).find(h, k, null);
}
```