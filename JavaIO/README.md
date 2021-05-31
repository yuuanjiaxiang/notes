# JAVA IO相关知识

[TOC]

## <font size = 5 color =blue>Java IO 与NIO的不同：</font>

1. `IO`是面向`stream`的，可以指定每次读取一定长度的字节，但是不能回退的访问数据，如果需要这么做则小把他存在缓存区中

   `NIO`是面向`buffer`的,数据先读取到`buffer`中，再做处理，可以在其中进行数据的前后访问

2. `IO`是`Blocking`的，阻塞式的，对于一个线程调用 read() 或 write() 时，该线程将被阻塞，直到有一些数据要读取，或者数据被完全写入。线程在此期间不能做任何其他事情

   `NIO`的`NO-Bloking`的，线程从`channel`读写数据，只用通知，在读写完成时才阻塞。也就是说，一个线程可以同时管理多个channel

https://blog.csdn.net/qwe6112071/article/details/71822185

## IO硬件原理

### IO硬件设备

包括输入设备与输出设备，根据工作方式的不同，可以划分为：

1. 字符设备：比如键盘，鼠标，显示器，扫描仪等，传输的信息比较少
2. 块设备：外部存储介质，用户通过这些设备实现程序和数据的长期保存。与字符设备相比，它们是以块为单位进行传输的，如磁盘、磁带和光盘等
3. 网络通信设备：网卡，调制解调器等，用来与远程设备进行通信

### IO四种控制方式

#### 1.直接程序控制

由CPU全程操作控制器进行数据读写，是CPU与IO设备串行的，但是由于他们的处理速度远不在一个量级上，并且CPU做了轮询，所以浪费了很多CPU的性能

![img](image\IOmathod1)

#### 2.中断驱动控制方式

![img](image\IOmathod2)

​	实际上是优化了轮询I/O控制器状态，在正常或者异常结束后，才给CPU发送请求，但是CPU依然参与了从设备往主存读写字的过程，每写一个字就要中断一次调起CPU，因此，可以在外围设备搞个缓存来减少中断次数

#### 3.直接存储器访问控制方式（DMA）

把CPU从一次一次的读写字中解放出来，让DMA去搞，CPU 接收到I/O 设备的DMA 请求时，它给I/0 控制器发出一条命令，启动DMA 控制器，然后继续其他工作。之后CPU 就把控制操作委托给DMA 控制器，由该控制器负责处理。DMA 控制器直接与存储器交互，传送整个数据块，每次传送一个字，这个过程不需要CPU 参与。传送完成后，DMA 控制器发送一个中断信号给处理器。因此只有在传送开始和结束时才需要CPU的参与(预处理【设置CR、MAR、DC等】和后处理【中断处理、唤醒因该I/O阻塞的进程程等】)

![img](image\IOmathod3)

#### 4.通道方式

DMA 方式与程序中断方式相比，使得CPU对IO的干预从字(字节) 为单位的减少到以数据块为单位。而且，每次 CPU干予时，并不要做数据拷贝，仅仅需要发一条启动 I/O 指 令 ，以及完成 I/O 结束中断处理。
但是，每发出一次I/O指令，只能读写一个数据块，如果用户希望一次读写多个离散的数据块，并能把它们传送到不同的内存区域，或相反时，则需要由CPU分别发出多条启动I/O指令及进行多次 I/O 中断处理才能完成。
通道方式进一步减少了CPU对I/O操作的干予，减少为对多个数据块，而不是仅仅一个数据块，及有关管理和控制的干予。
通道又称输入输出处理器。它能完成主存储器和外围设备之间的信息传送，与中央处理器并行地执行操作。
采用输入输出通道设计后，输入输出操作过程如下：

1. 中央处理机在执行主程序时遇到输入输出请求
2. 启动指定通道上选址的外围设备，一旦启动成功，通道开始控制外围设备进行操作。
3. 这时中央处理器就可执行其它任务并与通道并行工作，直到输入输出操作完成。
4. 通道发出操作结束中断时，中央处理器才停止当前工作，转向处理输入输出操作结束事件。

按照信息交换方式和加接设备种类不同，通道可分为三种类型：

**通道类型**
字节多路通道
它是为连接大量慢速外围设备，如软盘输入输出机、纸带输入输出机、卡片输入输出机、控制台打字机等设置的。以字节为单位交叉地工作，当为一台设备传送一个字节后，立即转去为另一台设备传送一个字节。在IBM370系统中，这样的通道可接 256 台设备。

**选择通道**
它用于连接磁带和磁盘快速设备。以成组方式工作，每次传送一批数据；故传送速度很高，但在这段时间只能为一台设备服务。每当一个输入输出操作请求完成后，再选择与通道相连接的另一设备。

**数组多路通道**
对于磁盘这样的外围设备，虽然传输信息很快，但是移臂定位时间很长。
1. 如果接在字节多路通道上，那么通道很难承受这样高的传输率；
2. 如果接在选择通道上，那么；磁盘臂移动所花费的较长时间内，通道只能空等。

数组多路通道可以解决这个矛盾，它先为一台设备执行一条通道命令，然后自动转换，为另一台设备执行一条通道命令。

对于连接在数组多路通道上的若干台磁盘机，可以启动它们同时进行移臂，查找欲访问的柱面，然后，按次序交叉传输一批批信息，这样就避免了移臂操作过长地占用通道。由于它在任一时刻只能为一台设备作数据传送服务，这类似于选择通道；但它不等整个通道程序执行结束就能执行另一设备的通道程序命令，这类似于字节多路通道。

