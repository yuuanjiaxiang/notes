# TreeMap

[TOC]

## 简介

`TreeMap`是一个直接由红黑树实现的结构，对于`Key`值的比较来排序，显然得到：

1.key的class必须实现`comparable`方法， 不能抛出`ClassCastException`异常

2.由于TreeMap实现了`Serializable`接口，所以默认的或者自定义的`comparator`也应该实现该接口

最重要的是，实现了`NavigableMap`，我理解为导航map,提供了各种操作map视图的操作

```java
public class TreeMap<K,V>
    extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Cloneable, java.io.Serializable{}
```



<font size = 5 color=#DC143C>具体的红黑树的操作在此不作赘述</font>  

<font size = 5 color=blue>remove(),put()最根本的操作是红黑树的操作，get()也是二叉搜索树比较直观的实现</font>



## successor() 查找下个节点

1. 在`containsValue()`从第一个节点开始successor遍历
2. 在`forEach()`从第一个节点开始successor遍历
3. `replaceAll()`从第一个节点开始successor遍历赋值新的value
4. `remove()`遍历找出Object删除

```java
static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
    // 首先明确，下个节点是比当前节点大的节点，为当前节点右节点的左叶子节点
    if (t == null)
        return null;
    else if (t.right != null) {
        Entry<K,V> p = t.right;
        while (p.left != null)
            p = p.left;
        return p;
    } else {
        Entry<K,V> p = t.parent;
        Entry<K,V> ch = t;
        // 当右节点为空，并且是父节点的右节点时，下个节点当前分支树的父节点
        while (p != null && ch == p.right) {
            ch = p;
            p = p.parent;
        }
        // 当右节点为空，并且是父节点的左节点时，下个节点当前节点的父节点
        return p;
    }
}
```



