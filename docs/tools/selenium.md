# Selenium for Python 

[TOC]

selenium是用于测试网站的自动化测试工具，不过这不重要，我们可以利用它来完成一下重复性的操作就行了

## 安装

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

可以通过页面元素的各种属性定义，常用CSS选择器选择，注意页面有些元素属性可能是随机生成的，不适合作为筛选条件

筛选多个使用`find_elements_by_xxx`

| 定位属性          | 语句                              | 含义                  |
| :---------------- | --------------------------------- | --------------------- |
| id                | find_element_by_id                | 通过元素id定位        |
| name              | find_element_by_name              | 通过元素name定位      |
| class name        | find_element_by_class_name        | 通过类名进行定位      |
| tag name          | find_element_by_tag_name          | 通过标签定位          |
| link text         | find_element_by_link_text         | 通过完整超链接定位    |
| partial link text | find_element_by_partial_link_text | 通过部分链接定位      |
| xpath             | find_element_by_xpath             | 通过xpath表达式定位   |
| css selector      | find_elements_by_css_selector     | 通过css选择器进行定位 |

例如

```python
dirver.find_element_by_css_selector("div[class='abc']")
```

### 鼠标事件

| 方法                   | 说明                                                         |
| ---------------------- | ------------------------------------------------------------ |
| ActionChains(driver)   | 构造ActionChains对象                                         |
| context_click()        | 执行鼠标悬停操作                                             |
| move_to_element(above) | 右击                                                         |
| double_click()         | 双击                                                         |
| drag_and_drop()        | 拖动                                                         |
| move_to_element(above) | 执行鼠标悬停操作                                             |
| context_click()        | 用于模拟鼠标右键操作， 在调用时需要指定元素定位              |
| perform()              | 执行所有 ActionChains 中存储的行为，可以理解成是对整个操作的提交动作 |

### 键盘事件

| 模拟键盘按键               | **说明**            |
| -------------------------- | ------------------- |
| send_keys(Keys.BACK_SPACE) | 删除键（BackSpace） |
| send_keys(Keys.SPACE)      | 空格键(Space)       |
| send_keys(Keys.TAB)        | 制表键(Tab)         |
| send_keys(Keys.ESCAPE)     | 回退键（Esc）       |
| send_keys(Keys.ENTER)      | 回车键（Enter）     |

| 模拟键盘按键               | **说明**            |
| -------------------------- | ------------------- |
|send_keys(Keys.CONTROL,‘a’)|	全选（Ctrl+A）|
|send_keys(Keys.CONTROL,‘c’)|	复制（Ctrl+C）|
|send_keys(Keys.CONTROL,‘x’)|	剪切（Ctrl+X）|
|send_keys(Keys.CONTROL,‘v’)|	粘贴（Ctrl+V）|
|send_keys(Keys.F1…Fn)|	键盘 F1…Fn|


### 窗口操作


| **方法**               | **说明**            |
| -------------------------- | ------------------- |
|current_window_handle|	获得当前窗口句柄|
|window_handles	|返回所有窗口的句柄到当前会话|
|switch_to.window()| 用于切换到相应的窗口，与上一节的switch_to.frame()类似  前者用于不同窗口的切换，后者用于不同表单之间的切换。 |
|close()| 关闭单个窗口 |
|quit()| 关闭所有窗口s |

### 设置等待

通过WebDriverWait设置等待事件

driver：浏览器驱动

timeout：等待时间

poll_frequency：检测的间隔时间，默认0.5s

```python
from selenium.webdriver.support.wait import WebDriverWait

driver = webdriver.Chrome(executable_path=driverfile_path)
WebDriverWait(driver,30,0.2).until(lambda x:x.find_element_by_css_selector("div[id='123']"))
```

