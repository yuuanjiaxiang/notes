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

