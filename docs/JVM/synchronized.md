[TOC]

## 起因

在周志明先生的《深入理解JAVA虚拟机》中，锁优化的章节讲到了，**当一个对象当前正处于偏向锁状态，又收到需要计算其一致性哈希码的请求时，它的偏向状态会被立刻撤销，并且锁会膨胀成重量级锁**，究其原因是因为对象头的Mark Word中，存储偏向锁的线程ID地址跟存储一致性Hash的比特位是冲突的

那么，如果对象轻量级锁，重量级锁的Mark Word难道不会跟Hash编码冲突吗，轻量级锁进行一致性Hash操作不会锁升级吗

做个实验来看一下

## Mark Word(HotSpot)

32位系统下存储为：

![32位MarkWord](F:\code\notes\docs\JVM\image\32位MarkWord.png)

64位系统实现：

![64位MarkWord](F:\code\notes\docs\JVM\image\64位MarkWord.png)

## 内存可视化工具

maven添加如下依赖，可以通过`ClassLayout.parseInstance(object).toPrintable()`方法直接在控制台打印对象内存

```xml
<dependency>
   <groupId>org.openjdk.jol</groupId>
   <artifactId>jol-core</artifactId>
   <version>0.9</version>
</dependency>
```

## 无锁状态

先定义一个简单的`Person`类

```java
public class Person {
    public final String name;

    public final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

创建一个简单对象，来看下内存hash前后的变化

```java
public class test {
    public static void main(String[] args) throws UnknownHostException {
        Person json = new Person("json", 14);
        System.out.println(ClassLayout.parseInstance(json).toPrintable());
        json.hashCode();
        System.out.println(ClassLayout.parseInstance(json).toPrintable());
    }
}
```

```
>>Integer.toBinaryString(json.hashCode())
>>1101011 10010110 01010001 11110011
```

![hash前后](F:\code\notes\docs\JVM\image\hash前后.png)

可以看到，在排除掉大小端的影响后，hash值是存在了26-56bit位上

偏向锁标记为0，锁标志位为01

## 偏向锁

```java
public class test {
    public static void main(String[] args) throws InterruptedException {
        //使线程进入可偏向状态
        Thread.sleep(5000);
        Person json = new Person("json", 14);
        System.out.println(ClassLayout.parseInstance(json).toPrintable());
        synchronized (json){//偏向锁
            System.out.println(ClassLayout.parseInstance(json).toPrintable());
            json.hashCode();//锁升级
            System.out.println(ClassLayout.parseInstance(json).toPrintable());
        }
    }
}

```

![image-20210905022049951](F:\code\notes\docs\JVM\image\偏向锁.png)

## 轻量级锁

```java
public static void main(String[] args) throws UnknownHostException{
        Person json = new Person("json", 14);
    	//未加锁
        System.out.println(ClassLayout.parseInstance(json).toPrintable());
        synchronized (json){
            //轻量级锁
            System.out.println(ClassLayout.parseInstance(json).toPrintable());
            json.hashCode();
            //升级为重量级锁
            System.out.println(ClassLayout.parseInstance(json).toPrintable());
        }
}
```

此时由于偏向锁不可用，直接加上轻量级锁，进行hash后，直接升级为重量级锁（开心，疑惑解开了）

![image-20210905012736777](F:\code\notes\docs\JVM\image\轻量级锁.png)

## 重量级锁

那么转为重量级锁后，hash值存到哪里去了呢？

这里引用R大的回答

这是一个针对HotSpot VM的锁实现的问题。
简单答案是：

- 当一个对象已经计算过identity hash code，它就无法进入偏向锁状态；
- 当一个对象当前正处于偏向锁状态，并且需要计算其identity hash code的话，则它的偏向锁会被撤销，并且锁会膨胀为重量锁；
- 重量锁的实现中，ObjectMonitor类里有字段可以记录非加锁状态下的mark word，其中可以存储identity hash code的值。或者简单说就是重量锁可以存下identity hash code。


请一定要注意，这里讨论的hash code都只针对identity hash code。用户自定义的hashCode()方法所返回的值跟这里讨论的不是一回事。
Identity hash code是未被覆写的 java.lang.Object.hashCode() 或者 java.lang.System.identityHashCode(Object) 所返回的值。

作者：RednaxelaFX
链接：https://www.zhihu.com/question/52116998/answer/133400077
来源：知乎
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

## 锁升级过程

现在再回过头来看锁升级过程，清晰多了

![锁升级过程](F:\code\notes\docs\JVM\image\锁升级过程.jpg)