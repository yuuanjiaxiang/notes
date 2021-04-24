# 安装

#### 安装Node.js

推荐v10.21.0版本的nodejs下载:

与gitbook完美兼容

https://blog.csdn.net/Lowerce/article/details/107579261

###### 查看源

```
npm get registry 
```

###### 更换源

```
npm config set registry http://registry.npm.taobao.org
```

#### 安装gitbook

```
 npm install gitbook-cli -g
```

###### 初始化gitbook项目

```
gitbook init 
```

会生成README.md  SUMMARY.md

SUMMARY是目录吗，按着添加文档就行

###### 部署服务

```
gitbook build
```

部署服务会默认build一下，build会自动生成对应的js,html文件

```
gitbook serve
```

参考:

https://zhuanlan.zhihu.com/p/34946169