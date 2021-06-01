# Buffer(以ByteBuffer为例)

[TOC]

## 简介

`Buffer`是一个线性有序集合，主要有三个标识：`capacity`,  ` limit`, `position`

**`capacity`**就是Buffer的容量，不为负且不改变

**`limit`**标识第一个不能读/写的元素索引，不为负且不大于`capacity`

**`position`**标识下一个要被读/写的元素索引不为负且不大于`limit`

**`mark`**当reset的时候回到标记的position的位置，不为负且不大于`position`

```0 <= mark <= position <= limit <= capacity```

对于每个非`boolean`的基础类型，都实现了一个Buffer子类，重写了`get`跟`put`方法，并且可以被对应的`channel`操作

每个`Buffer`都实现了`readable`但不一定实现了`writable`，并且是线程不安全的，对于并发多线程访问，应该实现同步控制

<font color=red>主要的三个方法：`clear`, `flip`, `rewind`</font>

<font size=6 color = gree>源码真的很简单，这里只搞懂怎么实现的图示就OK</font>

## 初始化

mark的默认值是-1，初始化主要检查0 <= mark <= position <= limit <= capacity关系

```java
private int mark = -1;
private int position = 0;
private int limit;
private int capacity;

Buffer(int mark, int pos, int lim, int cap) {       // package-private
    if (cap < 0)
        throw new IllegalArgumentException("Negative capacity: " + cap);
    this.capacity = cap;
    limit(lim);
    position(pos);
    if (mark >= 0) {
        if (mark > pos)
            throw new IllegalArgumentException("mark > position: ("
                                               + mark + " > " + pos + ")");
        this.mark = mark;
    }
}
```

示例：

```java
CharBuffer charBuffer = CharBuffer.allocate(16);
allocate 方法调用了HeapCharBuffer，默认mark=-1,position=0,limit=capacity;
```

![BufferInit](image\BufferInit.jpg)

## put()写入

每放一个元素，position后移一位

```java
charBuffer.put("Hello,World".toCharArray());
```

![BufferPut](image\BufferPut.jpg)

## flip()，切换为读就绪状态

```java
charBuffer.flip();
```

limit切到position位置，position切到首位

![BufferPut](image\BufferFlip.jpg)

## get()读取

每读取一次元素，positio后移一位

```java
charBuffer.get();//循环调用5次，读取"Hello"
```

![BufferPut](image\BufferGet.jpg)

## Mark()标记，reset()恢复标记位

mark()将mark置于position位置，reset()重新将position恢复到mark位置

```java
charBuffer.mark()
```

![BufferPut](image\BufferMark.jpg)



