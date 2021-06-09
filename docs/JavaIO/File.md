# Java IO 中的非流式部分

[TOC]

## File

File是java对于文件或者目录的一种描述对象，使硬盘上的资源变成Java可以操作的一个对象

File的创建是很简单的,提供一个路径或者文件名都可以：

```java
File file = new File("testfile.txt");
```

File的方法也比较容易理解，跟python里的os操作或者Linux命令行都很相似，这不是这里要了解的重点，只实现一下文件过滤的功能看一下：

### 文件过滤器

`FileFilter`跟`FilenameFilter`是两个文件过滤的接口，内部只不过是支持回调accept()`方法，通过覆写这个方法就可以实现自定义文件过滤器

```java
public class MyFileFilter implements FileFilter{
    @Override
    public boolean accept(File pathname) {
        return String.valueOf(pathname).endsWith("txt");
    }
}
```

### File读取通道





## RandomAccessFile

