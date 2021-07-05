# 安装

<!-- toc -->

## 安装Node.js

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

## 安装gitbook

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



## 插件

插件只要在book.json添加插件然后 `gitbook install` 安装一下即可(速度有点慢)或者使用`npm`安装

###  simple-page-toc  生成本页目录


`npm install gitbook-plugin-simple-page-toc`



```json
{
    "plugins" : [
        "simple-page-toc"
    ],
    "pluginsConfig": {
        "simple-page-toc": {
            "maxDepth": 3,
            "skipFirstH1": true
        }
    }
}
```

| 参数                | 说明                           |
| ------------------- | ------------------------------ |
| "maxDepth": 3       | 使用深度最多为maxdepth的标题。 |
| "skipFirstH1": true | 排除文件中的第一个h1级标题。   |

使用方法: 在需要生成目录的地方用```<!-- xxx -->```跟```top跟endtop```的标签括起来，全文都生成的话就在首尾添加


### Search Plus

支持中文搜索, 需要将默认的 `search` 和 `lunr` 插件去掉。

```json
{
    "plugins": ["-lunr", "-search", "search-plus"]
}
```
