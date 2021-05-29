# JAVA IO相关知识

[TOC]

## <font size = 5 color =blue>Java IO 与NIO的不同：</font>

1. `IO`是面向`stream`的，可以指定每次读取一定长度的字节，但是不能回退的访问数据，如果需要这么做则小把他存在缓存区中

   `NIO`是面向`buffer`的,数据先读取到`buffer`中，再做处理，可以在其中进行数据的前后访问

2. `IO`是`Blocking`的，阻塞式的，对于一个线程调用 read() 或 write() 时，该线程将被阻塞，直到有一些数据要读取，或者数据被完全写入。线程在此期间不能做任何其他事情

   `NIO`的`NO-Bloking`的，线程从`channel`读写数据，只用通知，在读写完成时才阻塞。也就是说，一个线程可以同时管理多个channel

https://blog.csdn.net/qwe6112071/article/details/71822185

## IO硬件原理