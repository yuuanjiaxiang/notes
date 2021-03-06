# ConcurrentHashMap 

[TOC]

<!-- toc -->

## 1.继承关系

```java
public class ConcurrentHashMap<K,V> extends AbstractMap<K,V>
    implements ConcurrentMap<K,V>, Serializable{}
```

跟 `map` 一样，继承自 `AbstractMap` ,但是实现的 `ConcurrentMap` 接口，`ConcurrentMap`与`Map`接口区别主要在于是否允许`null`值，以及对于并发的操作是否抛出 `ConcurrentModificationException ` 异常，以及通过synchronized保障一些操作（compute，merge）的原子性,只要保证操作的原子性后，那我们使用volatile标识变量，或者设定一个标识符，就可以保整并发状态下的一致性，可见性，原子性

[volatile关键字详解]: https://www.cnblogs.com/dolphin0520/p/3920373.html

### 核心原子性操作

```java
	//获得在i位置上的Node节点
    static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
        return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
    }
		//利用CAS算法设置i位置上的Node节点。之所以能实现并发是因为他指定了原来这个节点的值是多少
		//在CAS算法中，会比较内存中的值与你指定的这个值是否相等，如果相等才接受你的修改，否则拒绝你的修改
		//因此当前线程中的值并不是最新的值，这种修改可能会覆盖掉其他线程的修改结果  有点类似于SVN
    static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                        Node<K,V> c, Node<K,V> v) {
        return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
    }
		//利用volatile方法设置节点位置的值
    static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v) {
        U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
    }
```



## 2.put

```java
/** Implementation for put and putIfAbsent */
final V putVal(K key, V value, boolean onlyIfAbsent) {
    // key跟value都不允许空值
    if (key == null || value == null) throw new NullPointerException();
    // 计算Hash值
    int hash = spread(key.hashCode());
    // 记录链表长度
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        // 如果数组长度为空，进行初始化
        if (tab == null || (n = tab.length) == 0)
            tab = initTable();
        // 获取链表的第一个元素，为空则新建，使用CAS赋值
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            if (casTabAt(tab, i, null,
                         new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        // 如果f的Hash等于MOVED则扩容
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);
        // 如果头结点不为空，加锁
        else {
            V oldVal = null;
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    // 头结点hash>0为链表
                    if (fh >= 0) {
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    // 要是为Treebin，那就是红黑树
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {
                // 链表长度大于阈值，转化为树
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    // 这个里面巨复杂，大概就是对数组长度计数，并且实现了数组扩容
    addCount(1L, binCount);
    return null;
}
```

### 2.1treeifyBin（） 

```java
private final void treeifyBin(Node<K,V>[] tab, int index) {
    Node<K,V> b; int n, sc;
    if (tab != null) {
        // 如果数组长度没到转换阈值，那么只扩容
        if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
            // 这个方法中，主要使用CAS来更新数组size
            tryPresize(n << 1);
        // 原子操作获取当前节点，然后加锁，进行转换
        else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
            synchronized (b) {
                // 判断有没有被改变
                if (tabAt(tab, index) == b) {
                    // 还是遍历先生成一个双向链表
                    TreeNode<K,V> hd = null, tl = null;
                    for (Node<K,V> e = b; e != null; e = e.next) {
                        TreeNode<K,V> p =
                            new TreeNode<K,V>(e.hash, e.key, e.val,
                                              null, null);
                        if ((p.prev = tl) == null)
                            hd = p;
                        else
                            tl.next = p;
                        tl = p;
                    }
                    // 原子操作设置节点值
                    setTabAt(tab, index, new TreeBin<K,V>(hd));
                }
            }
        }
    }
}
```



## 3.get

其实get主函数里并没有加锁的操作，因为对于`volatile`变量来说，根据同步缓存协议，变更会使其他任务线程的缓存失效，从而从主存读取；

```java
public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    int h = spread(key.hashCode());
    // 如果数组不为空
    if ((tab = table) != null && (n = tab.length) > 0 &&
        // 使用原子操作 tabAt 来获取（并发核心）
        (e = tabAt(tab, (n - 1) & h)) != null) {
        // 找到并且hash值，key值都相等，则返回头结点val
        if ((eh = e.hash) == h) {
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                return e.val;
        }
        // 此时在迁移或扩容
        else if (eh < 0)
            return (p = e.find(h, key)) != null ? p.val : null;
        // 说明此时是个链表,直接遍历找出val;
        while ((e = e.next) != null) {
            if (e.hash == h &&
                ((ek = e.key) == key || (ek != null && key.equals(ek))))
                return e.val;
        }
    }
    return null;
}
```

## 4.remove(),replace()

都是调用的`replaceNode`方法，只不过找到val的时候，判断一下vlaue参数是否为`null`，`remove`直接删除，`replace`替换

```java
// value 是新的，cv 不为null的话要找到的node.val== cv 才替换
final V replaceNode(Object key, V value, Object cv) {
    int hash = spread(key.hashCode());
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        // 数组为空或者index位置为空，直接跳出
        if (tab == null || (n = tab.length) == 0 ||
            (f = tabAt(tab, i = (n - 1) & hash)) == null)
            break;
        // 如果在扩容，调用helpTransfer帮助扩容
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);
        else {
            V oldVal = null;
            boolean validated = false;
            // 锁住头结点，保证是操作的并发性
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    // 链表情况，循环查找
                    if (fh >= 0) {
                        validated = true;
                        for (Node<K,V> e = f, pred = null;;) {
                            K ek;
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                V ev = e.val;
                             	//remove跟replace的几种情况
                                if (cv == null || cv == ev ||
                                    (ev != null && cv.equals(ev))) {
                                    oldVal = ev;
                                    if (value != null)//replace
                                        e.val = value;
                                    else if (pred != null)//remove,且不是头结点
                                        pred.next = e.next;
                                    else//头结点使用原子操作
                                        setTabAt(tab, i, e.next);
                                }
                                break;
                            }
                            pred = e;
                            if ((e = e.next) == null)
                                break;
                        }
                    }
                    // 红黑树情况，查找使用findTreeNode,删除使用removeTreeNode，剩下的流程跟链表一样
                    else if (f instanceof TreeBin) {
                        validated = true;
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> r, p;
                        if ((r = t.root) != null &&
                            (p = r.findTreeNode(hash, key, null)) != null) {
                            V pv = p.val;
                            if (cv == null || cv == pv ||
                                (pv != null && cv.equals(pv))) {
                                oldVal = pv;
                                if (value != null)
                                    p.val = value;
                                else if (t.removeTreeNode(p))
                                    setTabAt(tab, i, untreeify(t.first));
                            }
                        }
                    }
                }
            }
            if (validated) {
                if (oldVal != null) {
                    if (value == null)
                        addCount(-1L, -1);
                    return oldVal;
                }
                break;
            }
        }
    }
    return null;
}
```

