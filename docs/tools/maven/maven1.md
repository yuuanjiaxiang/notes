[TOC]

# maven 学习笔记

## maven介绍

#### ant

Ant是一个将软件编译、测试、部署等步骤联系在一起的自动化工具，使用XML构建文件，Ant的主要用途是构建Java应用程序（tomcat）.

#### maven

maven是主要面向java的项目管理工具，当然也可以构建C#，Ruby,Scala

主要致力于两方面：构建(build)，依赖(dependencies),通过“约定大于配置”的思想，maven使用XML配置文件来管理任务，包括构建组件，外部依赖，构建顺序，目录和相关插件。

#### grandle

maven 约定大于配置，基于这种思想，使用一种基于[Groovy](https://link.zhihu.com/?target=http%3A//zh.wikipedia.org/wiki/Groovy)的[特定领域语言](https://link.zhihu.com/?target=http%3A//zh.wikipedia.org/w/index.php%3Ftitle%3D%E7%89%B9%E5%AE%9A%E9%A2%86%E5%9F%9F%E8%AF%AD%E8%A8%80%26action%3Dedit%26redlink%3D1)来声明项目设置

## 坐标

坐标的意义是什么呢，跟在笛卡尔坐标系的作用一样，使用坐标，我们可以把形形色色的jar包汇总在一起（maven仓库）然后使用坐标去寻找它的位置，来唯一且迅速的找到需要的包

#### groupId

maven项目隶属的组织，跟maven项目并不是一一对应的关系，比如org.sonatype.nexus,org.springframework;

#### artifactId

实际项目中的一个maven工程模块，一般以组织做前缀方便实际构建，比如mybatis-spring，spring-jdbc

#### version

maven有对应的一套完整的版本体系

*1.1.0-**SNAPSHOT*** 之类对应的是快照版本，用于团队内部开发迭代需求

*1.2.3-beta-4* 对应的是正式发布版本 **<主版本>.<次版本>.<增量版本>-<里程碑版本>** 其中增量版本与里程碑版本不是必须的

发布的正式版本应当 1)所有自动化测试全部通过；2）不依赖任何快照版本的插件和依赖；3）源码已提交到版本控制中心仓

#### packaging

maven项目的打包方式，包括**jar（默认）、war**

#### classifier

帮助定义构建输出的一些附属构件，比如使用

```java
<dependency>  
    <groupId>org.springframework</groupId>   
    <artifactId>spring-jdbc</artifactId>   
    <version>1.1.5</version>  
    <classifier>jdk15</classifier>    
</dependency>

<dependency>  
    <groupId>org.springframework</groupId>   
    <artifactId>spring-jdbc</artifactId>   
    <version>1.1.5</version>  
    <classifier>jdk13</classifier>    
</dependency>
```

可以分别生成 `spring-jdbc-1.1.5-jdk15.jar` 和 `spring-jdbc-1.1.5-jdk13.jar`

## 依赖范围

**\<scope>** 定义了对应的maven动作范围，首先要明白，把maven动作划分为编译，测试，运行

#### compile(默认)

在编译，测试，运行时都需要使用该依赖

#### test

仅在编译测试代码与运行测试代码的时候起作用，典型的是 ***JUnit***

#### provided

在编译跟测试时有效，运行时无效，常见应用与容器以及提供的以来，servlet-api

#### runtime

运行时以来，对编译跟运行无效，比如JDBC驱动实现

#### system

与provided范围一致，但是必须显示指定依赖路径，基本没用到过

## 依赖传递

![image-20220105010519919](image\image-20220105010519919.png)

如果A依赖于B，B依赖与C，则称A对于B是第一直接依赖，B对于C是第二直接依赖，A对于C是传递性依赖

传递性依赖的依赖范围如下：

|          | compile  | test | provided | runtime  |
| -------- | -------- | ---- | -------- | -------- |
| compile  | compile  | －   | －       | runtime  |
| test     | test     | －   | －       | test     |
| provided | provided | －   | provided | provided |
| runtime  | runtime  | －   | －       | runtime  |



#### 最短依赖原则

如果A->B->C->X(1.0) 跟 A->D->X(2.0) 是X对A的两条传递性依赖，那么路径短的会被解析（红色）

![image-20220107004633128](image\image-20220107004633128.png)

#### 优先声明原则

但是如果路径长度一样，那么从maven2.0.9开始，为了避免构建的不确定性，maven会选取在POM种优先声明的依赖

![image-20220107013541031](image\image-20220107013541031.png)

## 可选依赖

**\<optional>**标签来指明该依赖是否是可选依赖

```xml
	<dependency>
      <groupId>sample.ProjectB</groupId>
      <artifactId>Project-B</artifactId>
      <version>1.0</version>
      <scope>compile</scope>
      <optional>true</optional>
    </dependency>
```

当一个项目实现了两个特性，比如数据层的隔离工具包A，支持Mysql，redis数据库，那么A需要同时支持两种数据库的依赖，那么就需要将两个数据库驱动都打进服务包中，但是这两个数据库对用户来说又是互斥的，那么就需要将两个JDBC选为可选依赖，用户使用的时候来指定对应的依赖，引出可选依赖的特性：

- 可选依赖不被传递
- 其他项目需要时需要显式声明

<font color=red>但是</font>maven工程的设计也应当遵循软件设计原则，同一个工程不应当支持两种特性，这违反了**单一职责原则**,所以最好的情况是创建两个工程A-mysql 与A-redis,这样用户只需要依赖需要的工程而不需要显示指定JDBC依赖

## 排除依赖

**\<exclusion>**标签来指明需要排除的依赖

```xml
<dependency>
  <groupId>sample.ProjectB</groupId>
  <artifactId>Project-B</artifactId>
  <version>1.0</version>
  <scope>compile</scope>
  <exclusions>
      <exclusion>
          <groupId>sample.ProjectC</groupId>
          <artifactId>Project-C</artifactId>
      </exclusion>
  </exclusions>
</dependency>
```

如果我们依赖的项目有不稳定的SNAPSHOT版本，或者由于版权问题不能使用依赖，那么就需要排除掉对应的依赖，换上稳定合法的实现版本；