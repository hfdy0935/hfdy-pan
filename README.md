<div style="display:flex;flex-direction:column;align-items:center">
<img src="./frontend/src/assets/logo.svg" width="100"/><br/>
<h1>hfdy网盘</h1>
</div>


&emsp;&emsp;一个网盘项目，支持文件上传下载分享回收、大文件分块上传、断点续传和秒传、多级目录、视频转码、不同布局方式等。


## :hammer: 主要工具

|工具|用途|
|:--:|:--:|
|vue、vue-router、pinia|构件界面、路由、共享状态|
|arco-design|组件库|
|xgplayer、hls|播放音视频频|
|monaco、md-editor-v3|代码、markdown预览|
|vue-office|预览word、ppt、pdf、excel|
|spark-md5|文件分块计算MD5|
|springboot、mybatis-plus|后端主要构建工具|
|mysql、minio|数据、文件存储|
|redis|缓存|
|ffmpeg|视频转码|


简易流程图
![alt text](assets/未命名绘图.drawio.png)

## :rocket: 快速启动

```bash
# 项目根目录
docker compose up
# 访问localhost
```

## 2. :white_large_square: 截图


### 1. 登录

![alt text](assets/image.png)
![alt text](assets/image-1.png)
![alt text](assets/image-2.png)

### 2. 修改用户信息
![alt text](assets/image-3.png)
![alt text](assets/image-4.png)

### 2. 新建文件夹

![alt text](assets/image-5.png)
![alt text](assets/image-6.png)

### 3. 丰富的右键菜单

![alt text](assets/image-7.png)
![alt text](assets/image-8.png)
![alt text](assets/image-9.png)

![alt text](assets/image-10.png)

### 4. 布局

`list`
![alt text](assets/image-11.png)

`grid`
![alt text](assets/image-12.png)

`tree`
![alt text](assets/image-13.png)

### 5. 上传文件

![alt text](assets/image-14.png)
![alt text](assets/image-15.png)
视频轮询转码状态
![alt text](assets/image-16.png)

音乐可以单独上传歌词
![alt text](assets/image-17.png)

### 6. 预览文件

文件信息
![alt text](assets/image-18.png)
文件内容
![alt text](assets/image-19.png)
![alt text](assets/image-20.png)
![alt text](assets/image-21.png)
![alt text](assets/image-22.png)
![alt text](assets/image-23.png)
![alt text](assets/image-24.png)
![alt text](assets/image-25.png)
![alt text](assets/image-26.png)

### 7. 文件分享

![alt text](assets/image-27.png)
![alt text](assets/image-28.png)
提取
![alt text](assets/image-29.png)
![alt text](assets/image-30.png)
转存到新建文件夹4
![alt text](assets/image-31.png)
就有了
![alt text](assets/image-32.png)

分享设置
![alt text](assets/image-33.png)
分享文件管理（这里取消勾选图片）
![alt text](assets/image-34.png)
再看分享的文件
![alt text](assets/image-35.png)


### 8. 回收站

恢复或永久删除

### 9. 管理员用户管理

一些配置
![alt text](assets/image-36.png)
