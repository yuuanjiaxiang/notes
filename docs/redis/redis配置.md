## Redis单位

不区分大小写

1k => 1000 bytes

1kb => 1024 bytes

1m => 1000000 bytes

1mb => 1024*1024 bytes

1g => 1000000000 bytes

1gb => 1024*1024*1024 bytes



## include 导入

- ### **include**

当使用标准模板，并且需要对服务器进行一些自定义设置时可以用到

注意：redis总是最后的配置生效，所以最好是在开头就导入其他配置

```properties
include .\path\to\local.conf

include c:\path\to\other.conf
```



## network 网络

- ### **bind**

如果没有绑定接口，那么redis会监听当前服务所有可达的网络接口

`bind`后可以跟多个IP来绑定一个或多个IP地址

```properties
 bind 192.168.1.100 10.0.0.1 #绑定两个IP

 bind 127.0.0.1 ::1 #绑定Ip的端口
```

默认配置是

```properties
bind 127.0.0.1  #绑定本机下所有端口
```

如果去bind配置，将导致网络上的所有接口可见，危险!

- #### protected-mode

安全模式，为了防止redis实例被公开访问，默认开启

```properties
protected-mode yes
```

在保护模式打开时，如果1）没有使用bind显示指定IP 2）没有配置密码：

redis-server仅接受来自127.0.0.1和::1的连接 或者是Unix域名套接字，其他访问会报错

```
redis.clients.jedis.exceptions.JedisConnectionException: Failed connecting to host xxx.xxx.xxx.xxx:6379 
```

- ### **port**

监听端口来接受链接，默认是6379， 如果端口是0, Redis不会监听TCP socket

```properties
port 6379
```

- ### **tcp-backlog**

此参数确定了TCP连接中已完成队列(完成三次握手之后)的长度， 当然此值必须不大于Linux系统定义的/proc/sys/net/core/somaxconn值，默认是511，而Linux的默认参数值是128。当系统并发量大并且客户端速度缓慢的时候，可以将这二个参数一起参考设定。

```properties
tcp-backlog 511
```

- ###  **Unix socket.**

指定用来监听Unix套套接字的路径。没有默认值，所以在没有指定的情况下Redis不会监听Unix套接字

```properties
 unixsocket /tmp/redis.sock
 unixsocketperm 700
```

- ### **timeout**

客户端超过N秒空闲后关闭链接(0表示不关闭)

```properties
timeout 0
```

- ### **tcp-keepalive**

如果不为零，指定时间间隔来使用空闲链接向客户端发送ACK

​	1）检测无响应的对端

​	2）能让该链接的网络设备知道链接存活

```properties
tcp-keepalive 0 #0表示关闭
tcp-keepalive 60
```

##  GENERAL 通用配置

- ### **daemonize**

  设置守护线程，默认关闭，如果开启会写入一个`/var/run/redis.pid` 文件。

  WINDOWS不支持

  开启后，redis会在后台运行，此时redis将一直运行，除非手动kill该进程。

  ```properties
  daemonize no
  daemonize yes
  ```



- ### **pidfile**

  指定守护线程文件写入路径，不存在则会创建

  ```properties
  pidfile /var/run/redis.pid  #默认写入路径
  ```



- ### supervised

  可以通过upstart和systemd管理Redis守护进程，这个参数是和具体的操作系统相关的。

  [TODO]不是很明白，需要补习linux

  ```properties
  supervised no #默认没有监督互动
  supervised upstart #通过将Redis置于SIGSTOP模式来启动信号
  supervised systemd #signal systemd将READY = 1写入$ NOTIFY_SOCKET
  supervised auto #检测upstart或systemd方法基于 UPSTART_JOB或NOTIFY_SOCKET环境变量
  ```



- ### **loglevel**

  告警级别

  ```properties
  loglevel notice  #适量信息，使用线上生产环境
  loglevel debug   #大量信息，对开发/测试有用
  loglevel verbose #精简信息，只是比debug少一些
  loglevel warning #指挥答应严重告警信息
  ```



- ### **logfile**

  日志文件目录

  ```properties
  logfile ""   #控制台打印
  logfile /var/log/redis/redis.log   #指定目录保存
  ```



- ### **syslog**

  记录WINDOWS系统下的日志

  ```properties
  syslong-enabled no   #yes/no 开启/关闭
  syslog-ident redis   #指定在windows下的日志事件名，默认redis
  ```

- ### **databases**

  设置数据库数量，默认是16个，默认数据库是DB 0

  使用`select <dbid>` 来切换数据库

  ```properties
  databases 16    #数据库序号为0-15
  ```

## SNAPSHOTTING 快照持久化

RDB模式

[RDB如何应对快照过程中数据变更：写时复制](https://www.cnblogs.com/Courage129/p/14343136.html)	

- ### **save**

  `save <seconds> <changes>`

  如果 seconds 秒内 至少有 changes次操作发生，则保存rdb文件

  如果不使用save配置，则不会RDB持久化

  ```properties
  save ""    			#清空之前所有保存配置
  save 900 1
  save 300 10
  save 60 10000
  ```

- ### **stop-writes-on-bgsave-error**

  redis的RDB机制是调用forks启动子线程，将数据集写入到临时的RDB文件中，在替换原来的文件，并删除旧的RDB文件

  当这个过程中线程发生了错误，比如写入失败等情况，redis将停止接受写入，如果后台保存过程将再次开始工作，Redis 将自动允许再次写入。

  如果将stop-writes-on-bgsave-error 设置成no ，那么就算出错也会接着备份，那么需要一些监控机制来保证用户可以感受到这错误

  ```properties
  stop-writes-on-bgsave-error  yes     #默认开启
  ```
  
- ### **rdbcompression**
  在生成.rdb文件时，是否使用LZF压缩字符串，如果压缩则会提高CPU负载
  不使用的话则生成的rdb文件会比较大
    ```properties
  rdbcompression  yes     #默认开启
    ```

- ### **rdbchecksum**
  从RDB 5开始，会在结尾使用CRC64计算校验和，大概耗费10%的性能
  
  关闭的话会结尾校验和置为0
    ```properties
  rdbchecksum  yes     #默认开启
    ```

- ### **dbfilename**
  rdb保存文件名
    ```properties
  dbfilename dump.rdb     #默认名称
    ```

- ### **dir**
  rdb保存工作路径
    ```properties
  dir ./     #默认路径
    ```

## REPLICATION 主从复制

主从复制细节这里就不展开了，只看配置

```properties
slaveof <masterip> <masterport>  #绑定master的IP跟端口
```

- ### **masterauth**
  如果master使用requirepass进行了密码保护，则需要配置密码，否则master会拒接slave请求
    ```properties
  masterauth <master-password>     #默认路径
    ```


- ### **slave-serve-stale-data**
  当slave断开与master的链接的时候，或者slave同步仍未完成

    ```properties
  #开启的时候会根据过时数据回复客户端请求，如果当时是第一次同步，则数据集可能为空
  slave-serve-stale-data yes

  #关闭的时候，会统一按照SYNC with master in progress回复请求
  slave-serve-stale-data no
    ```


- ### **slave-read-only**
  配置slave 是否为只读，没见过配置slave为可写入的
    ```properties
  slave-read-only yes    #默认只读
    ```
  
- ### **repl-diskless-sync**
  配置主从同步时是否使用无盘复制

  当新建master-slave的关系或者重连时无法进行增量同步时(backlog找不到对应地offset)，需要进行一个”full-synchronization“全量同步，
  这个时候需要将dump.rdb文件从 master同步到slave,全量同步有两种方式：
  
  1)磁盘复制  master在接受到slave请求后会fork一个子进程，基于当前内存中已有的数据，创建一份最新的RDB文件写入磁盘，
  稍后(repl-diskless-sync-delay 规定时延收集其他需要同步的slave请求)主线程将rdb文件发送给所有的slaves
  
  2)无盘复制 主线程直接在内存中生成一个rdb文件然后传输给slave，如果有新的slave需要rdb文件，会在当前线程完成后再重新进行；master可以配置开始快照传输前的等待延迟，来时多个slave并行
  
  无盘复制主要用在磁盘速度慢而网络传输速度快的情况
  
    ```properties
  repl-diskless-sync no    #默认磁盘复制
    ```
  
- ### **repl-diskless-sync-delay**
  无盘复制情况下，配置时延，等待是否有其他slave一起参与传输
  
  单位为秒
    ```properties
  repl-diskless-sync-delay 5    #默认时延为5s
  repl-diskless-sync-delay 0    #时延为0表示立即开始
    ```

- ### **repl-ping-slave-period**
  从节点定时向主节点发送ping(心跳检测)，单位为秒
    ```properties
  repl-ping-slave-period 10    #默认10秒
    ```

- ### **repl-timeout**
  三种情况认为复制超时：
  1）slave角度，如果在repl-timeout时间内没有收到master SYNC传输的rdb snapshot数据，
  
  2）slave角度，在repl-timeout没有收到master发送的数据包或者ping。
  
  3）master角度，在repl-timeout时间没有收到REPCONF ACK确认信息。
  
  当redis检测到repl-timeout超时(默认值60s)，将会关闭主从之间的连接,redis slave发起重新建立主从连接的请求。
  
  对于内存数据集比较大的系统，可以增大repl-timeout参数。
    ```properties
  repl-timeout 60    #默认60秒
    ```

- ### **repl-disable-tcp-nodelay**
  [Nagle算法，TCP_NODELAY](https://en.wikipedia.org/wiki/Nagle's_algorithm)

  简单的来说如果开启，redis会用更少的数据包来向slave发送数据，同时用到的带宽也更小，但是会增加大约40ms的延迟
  
  Nagle算法的提出本质是为了在网络条件不好的情况下通过网络发送的数据包数量来提高TCP/IP网络效率的方法，如果是在网络条件不好，或者主备节点很多，主备网络间链路很长的情况下，开启这个算法也是行之有效的；
    ```properties
  repl-disable-tcp-nodelay no    #默认关闭
    ```

- ### **repl-ping-slave-period**
  设置复制积压大小。 积压是一个积累的缓冲区从机断开一段时间后的从机数据，
  这样当一个从机想再次重新连接，通常不需要完全重新同步，而是部分重新同步
  就足够了，只需传递从服务器断开连接后丢失的数据部分。 
  
  缓存积压区越大，就可以容忍slave断线更长时间，这样slave一恢复链接就可以开始部分同步操作
  ```properties
  repl-backlog-size 1mb    #默认1mb
  ```

- ### **repl-ping-slave-period**
  backlog是在master有slave链接的时候才会生成，同样的，如果所有的slave都断开链接并且长时间没有重新链接，
  那么backlog就会清空，这个参数就标识的最后一个salve断开链接后backlog仍然存活的时间Time to Live
  ```properties
  repl-backlog-ttl 3600    #默认3600S
  ```

- ### **slave-priority**
  slave-priority是个整数，当哨兵选举master的时候，越小的priority越会被选举;
  但是0表示不会被选举
  ```properties
  slave-priority 100    #默认100
  slave-priority 0      #0表示不会选举为master
  ```

- ### **min-slaves-xxx**
  当slave连接到master,且延迟<=M的数量少于N的时候，master可以拒绝写入
  
  延迟指的是从slave发送到master的ping时间(其实就是心跳机制) 如果配置了min-slaves-to-write，健康的slave的个数小于配置项N，mater就禁止写入。

  这个配置虽然不能保证N个slave都一定能接收到master的写操作，但是能避免没有足够健康的slave的时候，master不能写入来避免数据丢失 。
  
  设置为0关闭该功能。
  
  ```properties
  min-slaves-to-write 3  #最少有3个slave的延迟小于10才写入
  min-slaves-max-lag 10
  ```

## SECURITY 安全

  客户端在处理其他任何请求前，需要发出`AUTH <password>`命令，这在信任域可以被其他人访问时是有用的，但是一般
  并不用，因为大部分redis服务都是部署在自己服务器上
  由于redis每秒可以处理150K个请求，所以密码要足够强

  ```properties
  requirepass foobared
  ```

可以在共享环境中更改危险命令的名称。例如，CONFIG命令可能会被重命名为一些难以猜测的内容，以便它仍然可以用于内部使用的工具，但不能用于一般客户机。
也可以通过将命令重命名为空字符串来完全终止命令
  ```properties
    rename-command CONFIG b840fc02d524045429941cc15f59e41cb7be6c52+
    rename-command CONFIG ""
  ```

##LIMITS 客户端限制
设置同时连接的客户端最大数量，默认是10000台，
当达到最大连接数时会关闭新链接，返回`max number of clients reached`.
  ```properties
    maxclients 10000
  ```

##MEMORY MANAGEMENT 内存管理

###persistence-available
如果redis仅作为内存中的缓存而不进行任何持久化(AOF/RDB)，那么fork()子进程就没什么必要了,同时会禁用fork()操作的命令BGSAVE 和 BGREWRITEAOF。
  ```properties
    persistence-available [(yes)|no]
  ```

###maxmemory
设置内存最大字节数，如果达到内存限制时，redis会按照`maxmemory-policy`指定的策略来删除键值对，如果无法删除来减小内存，
redis会正常回复只读命令get等，但是对set,lpush等命令会报错；

  ```properties
    maxmemory <bytes>
  ```
###maxmemory-policy
到达最大内存时的删除策略
  ```properties
  volatile-lru  ->对"过期集合"中的数据采取LRU(近期最少使用)算法.如果对key使用"expire"指令指定了过期时间,那么此key将会被添加到"过期集合"中。将已经过期/LRU的数据优先移除.如果"过期集合"中全部移除仍不能满足内存需求,将OOM.
  allkeys-lru ->对所有的数据,采用LRU算法
  volatile-random ->对"过期集合"中的数据采取"随即选取"算法,并移除选中的K-V,直到"内存足够"为止. 如果如果"过期集合"中全部移除全部移除仍不能满足,将OOM
  allkeys-random ->对所有的数据,采取"随机选取"算法,并移除选中的K-V,直到"内存足够"为止
  volatile-ttl ->对"过期集合"中的数据采取TTL算法(最小存活时间),移除即将过期的数据.
  noeviction ->不做任何干扰操作,直接返回OOM异常

  maxmemory-policy noeviction #默认策略

  使用以下任何命令时，只要没有合适的键删除则报错: set setnx setex append \
  incr decr rpush lpush rpushx lpushx linsert lset rpoplpush sadd\
  sinter sinterstore sunion sunionstore sdiff sdiffstore zadd zincrby\
  zunionstore zinterstore hset hsetnx hmset hincrby incrby decrby\
  getset mset msetnx exec sort

  ```

###maxmemory-samples
LRU算法和LLT算法并不是精确的，而是为了节省内存使用的近似算法，每次选取一个样本集合来做。
通过这个配置可以更改每个样本集合的大小；

如果样本大则更耗费CPU资源但是更精确，样本小则更快，但是准确性差

  ```properties
  maxmemory-samples 5
  ```

## 追加模式 AOF配置

AOF跟RDB具体的区别不做展开，只用知道如果AOF与RDB同时开启是没问题的，系统会优先加载AOF文件来保证更好的耐用性

###appendonly
是否打开AOF配置
  ```properties
  appendonly no  #默认关闭
  ```

###appendfilename
生成的AOF文件名，默认为`appendonly.aof`
  ```properties
  appendfilename "appendonly.aof"  #AOF文件名，默认值
  ```

###appendfsync
`fsync()`命令通知操作系统把数据写入磁盘而不是写入缓存区；某些操作系统会立即刷新数据到磁盘，有些操作系统只是尽快尝试去做这件事

redis的appendfsync命令指定提供了三种不同的模式：

`no`: 从来不主动调用`fsync()`，而是等操作系统自己调用，是最快的.

`always`: 每条写命令都会调用`fsync()`存入磁盘，虽然慢但是最安全.

`everysec`: 每秒调用一次`fsync()`，折中的方案，最坏情况下只会丢失1s的数据.
```properties
  appendfsync always
  appendfsync everysec  #默认配置
  appendfsync no
```

###no-appendfsync-on-rewrite
bgrewriteaof机制，在一个子进程中进行aof的重写，从而不阻塞主进程对其余命令的处理，同时解决了aof文件过大问题。

现在问题出现了，同时在执行bgrewriteaof操作和主进程写aof文件的操作，两者都会操作磁盘，而bgrewriteaof往往会涉及大量磁盘操作，这样就会造成主进程在写aof文件的时候出现阻塞的情形，现在no-appendfsync-on-rewrite参数出场了。如果该参数设置为no，是最安全的方式，不会丢失数据，但是要忍受阻塞的问题。如果设置为yes呢？这就相当于将appendfsync设置为no，这说明并没有执行磁盘操作，只是写入了缓冲区，因此这样并不会造成阻塞（因为没有竞争磁盘），但是如果这个时候redis挂掉，就会丢失数据。丢失多少数据呢？在linux的操作系统的默认设置下，最多会丢失30s的数据。

因此，如果应用系统无法忍受延迟，而可以容忍少量的数据丢失，则设置为yes。如果应用系统无法忍受数据丢失，则设置为no。
```properties
  no-appendfsync-on-rewrite no
```

###auto-aof-rewrite
Redis可以自动重写AOF文件，或者使用命令`Bgrewriteaof `手动触发

当redis最后一次重写AOF文件时(如果是刚开始没有触发重写，那就使用AOF文件初始大小)，会记录下这个文件的大小，
重写发生后会用当前AOF文件大小与记录的大小做比较：

1)增量大小超过了记录大小的预设百分比，触发重写；**为0则表示关闭重写功能**

2)指定重写的最小文件大小，不满足则不触发重写，避免频繁重写耗费性能

```properties
auto-aof-rewrite-percentage 100   #触发重写的百分比
auto-aof-rewrite-min-size 64mb    #最小重写文件大小
```

###aof-load-truncated
在启动Redis进程的时候，加载AOF文件到内存过程中，可能会发现AOF文件末尾被截断了，尤其是Redis进程异常崩溃后(特别是在不使用 data=ordered 选项挂载ext4文件系统时。但是Redis本身崩溃而操作系统正常运行则不会出现该情况)；

可以使用这个配置来选择抛出异常退出或者尽可能多的加载数据(默认配置)

yes ：末尾被截断的 AOF 文件将会被加载，并打印日志通知用户。

no ：服务器将报错并拒绝启动。

这时用户需要使用redis-check-aof 工具修复AOF文件，再重新启动。
```properties
  aof-load-truncated yes
```

###aof-use-rdb-preamble
开启AOF-RDB混合持久化，生成的AOF文件前半段是RDB格式的全量数据后半段是AOF格式的增量数据
  ```properties
  aof-use-rdb-preamble yes
  ```
##LUA 脚本
###lua-time-limit

LUA脚本的最大执行时间，单位为毫秒

当脚本运行时间超过这一限制后，Redis 将开始接受其他命令但不会执行（以确保脚本的原子性，因为此时脚本并没有被终止），而是会返回“BUSY”错误。

Redis 提供了一个script kill 的命令来中止脚本的执行

如果当前执行的Lua 脚本对Redis 的数据进行了修改（SET、DEL 等），那么通过script kill 命令是不能终止脚本运行的。
遇到这种情况，只能通过shutdown nosave 命令来强行终止redis。shutdown nosave 和shutdown 的区别在于shutdown nosave 不会进行持久化操作，意味着发生在上一次快照后的数据库修改都会丢失。

设置为0或负值将会以无报错的情况进行无限制执行。

```properties
  lua-time-limit 5000
  ```

##REDIS CLUSTER 集群

一般的Redis实例不能作为集群的节点，只有配置了`cluster-enabled`才能作为集群的节点
  ```properties
  cluster-enabled yes
  ```

集群的每个节点都有一个配置文件，是不允许手动编辑的；配置文件是由Redis节点创建和更新的，要确保同一集群中
的每个节点实例配置文件名都不重复
  ```properties
cluster-config-file nodes-6379.conf
  ```

集群节点的超时时间是以毫秒为单位的，许多内部的时间限制是超时时间的倍数
 ```properties
cluster-node-timeout 15000
  ```

如果主节点失败了，从节点会避免故障转移，如果数据太旧

数据太旧的定义是执行如下两个检查：

1)如果有多个从节点可以实现故障转移，从节点们会互相通信，根据offset提供最新的从节点

2)每个从节点都计算上次与Master进行通信的时间(ping,Master命令下发，断开链接等)，如果上次通信时间太久的话就不考虑在当前从节点进行故障转移;

There is no simple way for a slave to actually have a exact measure of
its "data age", so the following two checks are performed:

1) If there are multiple slaves able to failover, they exchange messages
   in order to try to give an advantage to the slave with the best
   replication offset (more data from the master processed).
   Slaves will try to get their rank by offset, and apply to the start
   of the failover a delay proportional to their rank.

2) Every single slave computes the time of the last interaction with
   its master. This can be the last ping or command received (if the master
   is still in the "connected" state), or the time that elapsed since the
   disconnection with the master (if the replication link is currently down).
   If the last interaction is too old, the slave will not try to failover
   at all.

The point "2" can be tuned by user. Specifically a slave will not perform
the failover if, since the last interaction with the master, the time
elapsed is greater than:

(node-timeout * slave-validity-factor) + repl-ping-slave-period

So for example if node-timeout is 30 seconds, and the slave-validity-factor
is 10, and assuming a default repl-ping-slave-period of 10 seconds, the
slave will not try to failover if it was not able to talk with the master
for longer than 310 seconds.

A large slave-validity-factor may allow slaves with too old data to failover
a master, while a too small value may prevent the cluster from being able to
elect a slave at all.

For maximum availability, it is possible to set the slave-validity-factor
to a value of 0, which means, that slaves will always try to failover the
master regardless of the last time they interacted with the master.
(However they'll always try to apply a delay proportional to their
offset rank).

Zero is the only value able to guarantee that when all the partitions heal
the cluster will always be able to continue.

cluster-slave-validity-factor 10

Cluster slaves are able to migrate to orphaned masters, that are masters
that are left without working slaves. This improves the cluster ability
to resist to failures as otherwise an orphaned master can't be failed over
in case of failure if it has no working slaves.

Slaves migrate to orphaned masters only if there are still at least a
given number of other working slaves for their old master. This number
is the "migration barrier". A migration barrier of 1 means that a slave
will migrate only if there is at least 1 other working slave for its master
and so forth. It usually reflects the number of slaves you want for every
master in your cluster.

Default is 1 (slaves migrate only if their masters remain with at least
one slave). To disable migration just set it to a very large value.
A value of 0 can be set but is useful only for debugging and dangerous
in production.

cluster-migration-barrier 1

By default Redis Cluster nodes stop accepting queries if they detect there
is at least an hash slot uncovered (no available node is serving it).
This way if the cluster is partially down (for example a range of hash slots
are no longer covered) all the cluster becomes, eventually, unavailable.
It automatically returns available as soon as all the slots are covered again.

However sometimes you want the subset of the cluster which is working,
to continue to accept queries for the part of the key space that is still
covered. In order to do so, just set the cluster-require-full-coverage
option to no.

cluster-require-full-coverage yes

In order to setup your cluster make sure to read the documentation
available at http://redis.io web site.

SLOW LOG

The Redis Slow Log is a system to log queries that exceeded a specified
execution time. The execution time does not include the I/O operations
like talking with the client, sending the reply and so forth,
but just the time needed to actually execute the command (this is the only
stage of command execution where the thread is blocked and can not serve
other requests in the meantime).

You can configure the slow log with two parameters: one tells Redis
what is the execution time, in microseconds, to exceed in order for the
command to get logged, and the other parameter is the length of the
slow log. When a new command is logged the oldest one is removed from the
queue of logged commands.

The following time is expressed in microseconds, so 1000000 is equivalent
to one second. Note that a negative number disables the slow log, while
a value of zero forces the logging of every command.
slowlog-log-slower-than 10000

There is no limit to this length. Just be aware that it will consume memory.
You can reclaim memory used by the slow log with SLOWLOG RESET.
slowlog-max-len 128

LATENCY MONITOR

The Redis latency monitoring subsystem samples different operations
at runtime in order to collect data related to possible sources of
latency of a Redis instance.

Via the LATENCY command this information is available to the user that can
print graphs and obtain reports.

The system only logs operations that were performed in a time equal or
greater than the amount of milliseconds specified via the
latency-monitor-threshold configuration directive. When its value is set
to zero, the latency monitor is turned off.

By default latency monitoring is disabled since it is mostly not needed
if you don't have latency issues, and collecting data has a performance
impact, that while very small, can be measured under big load. Latency
monitoring can easily be enabled at runtime using the command
"CONFIG SET latency-monitor-threshold <milliseconds>" if needed.
latency-monitor-threshold 0

EVENT NOTIFICATION

Redis can notify Pub/Sub clients about events happening in the key space.
This feature is documented at http://redis.io/topics/notifications

For instance if keyspace events notification is enabled, and a client
performs a DEL operation on key "foo" stored in the Database 0, two
messages will be published via Pub/Sub:

PUBLISH __keyspace@0__:foo del
PUBLISH __keyevent@0__:del foo

It is possible to select the events that Redis will notify among a set
of classes. Every class is identified by a single character:

K     Keyspace events, published with __keyspace@<db>__ prefix.
E     Keyevent events, published with __keyevent@<db>__ prefix.
g     Generic commands (non-type specific) like DEL, EXPIRE, RENAME, ...
$     String commands
l     List commands
s     Set commands
h     Hash commands
z     Sorted set commands
x     Expired events (events generated every time a key expires)
e     Evicted events (events generated when a key is evicted for maxmemory)
A     Alias for g$lshzxe, so that the "AKE" string means all the events.

The "notify-keyspace-events" takes as argument a string that is composed
of zero or multiple characters. The empty string means that notifications
are disabled.

Example: to enable list and generic events, from the point of view of the
event name, use:

notify-keyspace-events Elg

Example 2: to get the stream of the expired keys subscribing to channel
name __keyevent@0__:expired use:

notify-keyspace-events Ex

By default all notifications are disabled because most users don't need
this feature and the feature has some overhead. Note that if you don't
specify at least one of K or E, no events will be delivered.
notify-keyspace-events ""

ADVANCED CONFIG

Hashes are encoded using a memory efficient data structure when they have a
small number of entries, and the biggest entry does not exceed a given
threshold. These thresholds can be configured using the following directives.
hash-max-ziplist-entries 512
hash-max-ziplist-value 64

Lists are also encoded in a special way to save a lot of space.
The number of entries allowed per internal list node can be specified
as a fixed maximum size or a maximum number of elements.
For a fixed maximum size, use -5 through -1, meaning:
-5: max size: 64 Kb  <-- not recommended for normal workloads
-4: max size: 32 Kb  <-- not recommended
-3: max size: 16 Kb  <-- probably not recommended
-2: max size: 8 Kb   <-- good
-1: max size: 4 Kb   <-- good
Positive numbers mean store up to _exactly_ that number of elements
per list node.
The highest performing option is usually -2 (8 Kb size) or -1 (4 Kb size),
but if your use case is unique, adjust the settings as necessary.
list-max-ziplist-size -2

Lists may also be compressed.
Compress depth is the number of quicklist ziplist nodes from *each* side of
the list to *exclude* from compression.  The head and tail of the list
are always uncompressed for fast push/pop operations.  Settings are:
0: disable all list compression
1: depth 1 means "don't start compressing until after 1 node into the list,
going from either the head or tail"
So: [head]->node->node->...->node->[tail]
[head], [tail] will always be uncompressed; inner nodes will compress.
2: [head]->[next]->node->node->...->node->[prev]->[tail]
2 here means: don't compress head or head->next or tail->prev or tail,
but compress all nodes between them.
3: [head]->[next]->[next]->node->node->...->node->[prev]->[prev]->[tail]
etc.
list-compress-depth 0

Sets have a special encoding in just one case: when a set is composed
of just strings that happen to be integers in radix 10 in the range
of 64 bit signed integers.
The following configuration setting sets the limit in the size of the
set in order to use this special memory saving encoding.
set-max-intset-entries 512

Similarly to hashes and lists, sorted sets are also specially encoded in
order to save a lot of space. This encoding is only used when the length and
elements of a sorted set are below the following limits:
zset-max-ziplist-entries 128
zset-max-ziplist-value 64

HyperLogLog sparse representation bytes limit. The limit includes the
16 bytes header. When an HyperLogLog using the sparse representation crosses
this limit, it is converted into the dense representation.

A value greater than 16000 is totally useless, since at that point the
dense representation is more memory efficient.

The suggested value is ~ 3000 in order to have the benefits of
the space efficient encoding without slowing down too much PFADD,
which is O(N) with the sparse encoding. The value can be raised to
~ 10000 when CPU is not a concern, but space is, and the data set is
composed of many HyperLogLogs with cardinality in the 0 - 15000 range.
hll-sparse-max-bytes 3000

Active rehashing uses 1 millisecond every 100 milliseconds of CPU time in
order to help rehashing the main Redis hash table (the one mapping top-level
keys to values). The hash table implementation Redis uses (see dict.c)
performs a lazy rehashing: the more operation you run into a hash table
that is rehashing, the more rehashing "steps" are performed, so if the
server is idle the rehashing is never complete and some more memory is used
by the hash table.

The default is to use this millisecond 10 times every second in order to
actively rehash the main dictionaries, freeing memory when possible.

If unsure:
use "activerehashing no" if you have hard latency requirements and it is
not a good thing in your environment that Redis can reply from time to time
to queries with 2 milliseconds delay.

use "activerehashing yes" if you don't have such hard requirements but
want to free memory asap when possible.
activerehashing yes

The client output buffer limits can be used to force disconnection of clients
that are not reading data from the server fast enough for some reason (a
common reason is that a Pub/Sub client can't consume messages as fast as the
publisher can produce them).

The limit can be set differently for the three different classes of clients:

normal -> normal clients including MONITOR clients
slave  -> slave clients
pubsub -> clients subscribed to at least one pubsub channel or pattern

The syntax of every client-output-buffer-limit directive is the following:

client-output-buffer-limit <class> <hard limit> <soft limit> <soft seconds>

A client is immediately disconnected once the hard limit is reached, or if
the soft limit is reached and remains reached for the specified number of
seconds (continuously).
So for instance if the hard limit is 32 megabytes and the soft limit is
16 megabytes / 10 seconds, the client will get disconnected immediately
if the size of the output buffers reach 32 megabytes, but will also get
disconnected if the client reaches 16 megabytes and continuously overcomes
the limit for 10 seconds.

By default normal clients are not limited because they don't receive data
without asking (in a push way), but just after a request, so only
asynchronous clients may create a scenario where data is requested faster
than it can read.

Instead there is a default limit for pubsub and slave clients, since
subscribers and slaves receive data in a push fashion.

Both the hard or the soft limit can be disabled by setting them to zero.
client-output-buffer-limit normal 0 0 0
client-output-buffer-limit slave 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60

Redis calls an internal function to perform many background tasks, like
closing connections of clients in timeot, purging expired keys that are
never requested, and so forth.

Not all tasks are perforemd with the same frequency, but Redis checks for
tasks to perform according to the specified "hz" value.

By default "hz" is set to 10. Raising the value will use more CPU when
Redis is idle, but at the same time will make Redis more responsive when
there are many keys expiring at the same time, and timeouts may be
handled with more precision.

The range is between 1 and 500, however a value over 100 is usually not
a good idea. Most users should use the default of 10 and raise this up to
100 only in environments where very low latency is required.
hz 10

When a child rewrites the AOF file, if the following option is enabled
the file will be fsync-ed every 32 MB of data generated. This is useful
in order to commit the file to the disk more incrementally and avoid
big latency spikes.
aof-rewrite-incremental-fsync yes
