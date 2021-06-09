# Reader/Writer

[TOC]

# Reader

`Reader`是抽象类，子类主要实现了<font color=red>`read`,`close`</font>方法，为了效率及更多功能，才实现了更多方法；

Reader的抽象类与Stream不同的是，定义了lock，而stream是子类实现的时候使用synchronized来加锁的

```java
protected Object lock;

// 默认使用对象自己做锁
protected Reader() {
    this.lock = this;
}

// 支持用户自定义锁
protected Reader(Object lock) {
    if (lock == null) {
        throw new NullPointerException();
    }
    this.lock = lock;
}
```

## read()

与`InputStram`不同的是 ，`read()`核心是读取到一个数组里，读取单个字符则是使用一个size为1的数组实现

```java
//最大可跳过长度InputStream是2048，Reader是8192
private static final int maxSkipBufferSize = 8192;
```

```java
public int read(java.nio.CharBuffer target) throws IOException {
    // limit - position 来获取可用长度
    int len = target.remaining();
    char[] cbuf = new char[len];
    int n = read(cbuf, 0, len);
    // 大于0标识读到了
    if (n > 0)
        target.put(cbuf, 0, n);
    return n;
}

public int read() throws IOException {
    char cb[] = new char[1];
    if (read(cb, 0, 1) == -1)
        return -1;
    else
        return cb[0];
}

// 需要子类实现的关键方法，读取一个字符数组，返回读取到的长度
public int read(char cbuf[]) throws IOException {
    return read(cbuf, 0, cbuf.length);
}
```

## skip()

跟InputStream的打大同小异，使用一个buffer来存要跳过的字符，返回跳过的长度

```java
public long skip(long n) throws IOException {
    //先判断大小
    if (n < 0L)
        throw new IllegalArgumentException("skip value is negative");
    int nn = (int) Math.min(n, maxSkipBufferSize);
    //加锁读
    synchronized (lock) {
        if ((skipBuffer == null) || (skipBuffer.length < nn))
            skipBuffer = new char[nn];
        //r是仍需要读取的长度，大于0时候一直读
        long r = n;
        while (r > 0) {
            int nc = read(skipBuffer, 0, (int)Math.min(r, nn));
            // 遇到读不出来中止
            if (nc == -1)
                break;
            r -= nc;
        }
        return n - r;
    }
}
```

## mark(),reset(),markSupported(),close()

与InputStream没什么不同

# Writer

writer实在没什么新意，大部分与OutputStream与Reader的思想是一致的，就不再啰嗦了

子类必须实现`write(char[], int, int), flush(), close()`