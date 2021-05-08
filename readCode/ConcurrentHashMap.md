# ConcurrentHashMap 

<!-- toc -->

## 1.继承关系

跟 `map` 一样，继承自 `AbstractMap` ,但是实现的 `ConcurrentMap` 接口，`ConcurrentMap`与`Map`接口区别主要在于是否允许`null`值，以及对于并发的操作是否抛出 `ConcurrentModificationException ` 异常，以及保障一些操作（compute，merge）的原子性

```java
public class ConcurrentHashMap<K,V> extends AbstractMap<K,V>
    implements ConcurrentMap<K,V>, Serializable{}
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
        // 获取链表的第一个元素，为空则新建
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
    addCount(1L, binCount);
    return null;
}
```

## 3.get