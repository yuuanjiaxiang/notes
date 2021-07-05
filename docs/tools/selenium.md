# Selenium for Python 

selenium是用于测试网站的自动化测试工具，不过这不重要，我们可以利用它来完成一下重复性的操作就行了

https://blog.csdn.net/weixin_36279318/article/details/79475388

# 安装

1. python库：`pip install Selenium`

2. 浏览器驱动：[Chrome驱动文件下载](https://chromedriver.storage.googleapis.com/index.html?path=2.35/)

   ​					   [Firefox驱动文件下载](https://github.com/mozilla/geckodriver/releases)

3. 配置环境变量：直接将驱动所在文件夹加到Path值

## 操作指南

### 启动

```python
driver = webdriver.Chrome(executable_path="D:\chromedriver")
driver.get("https://www.baidu.com")
```

### 查找元素

可以通过ID，

| id                |      |      |
| :---------------- | ---- | ---- |
| name              |      |      |
| class name        |      |      |
| tag name          |      |      |
| link text         |      |      |
| partial link text |      |      |
| xpath             |      |      |
| css selector      |      |      |



```python
dirver.find_element_by_css_selector("div[class='abc']")
```