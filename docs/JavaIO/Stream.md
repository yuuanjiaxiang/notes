#  Stream

[TOC]

## InputStream

`InputStream`是所有从文件中获取字节流的超类，子类必须实现`read()`方法来获取下一个字节

```java
//返回的是0-255的数字，如果没有下一个字节返回-1
public abstract int read() throws IOException;
```

###  read(byte b[], int off, int len)

```java
//向b[]数组中读入从off位置开始，len长度的字节，返回读到的字节数，没有返回0，否则返回-1；
//其实是反复调用了read()方法，对于[b[0],b[off])∩(b[off+len],b[b.len-1]]没有影响
public int read(byte b[], int off, int len) throws IOException {
    if (b == null) {
        throw new NullPointerException();
    } else if (off < 0 || len < 0 || len > b.length - off) {
        throw new IndexOutOfBoundsException();
    } else if (len == 0) {
        return 0;
    }

    int c = read();
    if (c == -1) {
        return -1;
    }
    b[off] = (byte)c;

    int i = 1;
    try {
        for (; i < len ; i++) {
            c = read();
            if (c == -1) {
                break;
            }
            b[off + i] = (byte)c;
        }
    } catch (IOException ee) {
    }
    return i;
}
```

### skip(long n)

顾名思义，是跳过读取n个字节，通过将n个字节通过read()方法读入一个数组实现，返回值是跳过的长度len,这个len有可能小于n,比如读取到了文件的末尾，所以最后要判断一下

```Java
public long skip(long n) throws IOException {
    // remaining定义仍需要读的长度
    long remaining = n;
    int nr;

    if (n <= 0) {
        return 0;
    }
    //定义一个读取跳过字节的数组，大小不能大于最大跳过数2048
    int size = (int)Math.min(MAX_SKIP_BUFFER_SIZE, remaining);
    byte[] skipBuffer = new byte[size];
    while (remaining > 0) {
        //往数组里读，nr作为读到的长度，如果小于0说明读到末尾，退出循环
        nr = read(skipBuffer, 0, (int)Math.min(size, remaining));
        if (nr < 0) {
            break;
        }
        remaining -= nr;
    }
    //返回的是已读懂到的长度
    return n - remaining;
}
```

### available()

```java
// 返回stream中可被读取或跳过的字节数的估计值，并且不会被阻塞
public int available() throws IOException {
    return 0;
}
```

### close()

这就是为什么`InputStream`实现了`Closeable`接口的原因：在java硬件基础中我们可以看到，其实不管是什么样的读取控制方式，始终是由一个线程来控制资源（文件或者网络），不管是CPU控制的还是I/O控制器控制的，在read()等操作方法中，会造成文件资源的阻塞，所以，必须在操作完成后关闭资源；

```java
public void close() throws IOException {}
```

### mark(),reset(),markSupported()

```java
// 标记位置，readlimit是mark失效前的最大可读长度
public synchronized void mark(int readlimit) {}
// 复位到标记位
public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
//判断mark/reset是否可用
public boolean markSupported() {
        return false;
   }
```

## OutputStream

OutputStream与InputStream简单很多，核心只有write方法与实现了`Flushable`接口，依然是需要子类必须实现单字节的`write()`

###  write(byte b[], int off, int len)

```java
//多字节的数组写入，依然是进行边界条件判断，然后再写入，write是不需要返回值的
public void write(byte b[], int off, int len) throws IOException {
    if (b == null) {
        throw new NullPointerException();
    } else if ((off < 0) || (off > b.length) || (len < 0) ||
               ((off + len) > b.length) || ((off + len) < 0)) {
        throw new IndexOutOfBoundsException();
    } else if (len == 0) {
        return;
    }
    for (int i = 0 ; i < len ; i++) {
        write(b[off + i]);
    }
}
```

### flush()

flush主要是将缓存区的残留内容输出，但是如果说子类是关于底层操作系统的抽象，比如是文件，那只能保证提供给操作系统去写，不能保证写入成功

```Java
public void flush() throws IOException {
}
```