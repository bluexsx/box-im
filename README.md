
###  **盒子IM** 
![MIT协议](https://img.shields.io/badge/license-MIT-red)
[![star](https://gitee.com/bluexsx/box-im/badge/star.svg)](https://gitee.com/bluexsx/box-im) 
[![star](https://img.shields.io/github/stars/bluexsx/box-im.svg?style=flat&logo=GitHub)](https://github.com/bluexsx/box-im) 
<a href="#加入交流群"><img src="https://img.shields.io/badge/QQ交流群-green.svg?style=plasticr"></a>

1. 盒子IM是一个仿微信实现的网页版聊天软件，不依赖任何第三方收费组件。
1. 支持私聊、群聊、离线消息、发送语音、图片、文件、已读未读、群@等功能
1. 支持单人、多人音视频通话(基于原生webrtc实现,需要ssl证书)
1. uniapp端兼容app、h5、微信小程序,可与web端同时在线，并保持消息同步
1. 后端采用springboot+netty实现，网页端使用vue，移动端使用uniapp
1. 服务器支持集群化部署，每个im-server仅处理自身连接用户的消息


详细文档：https://www.yuque.com/u1475064/mufu2a


#### 近期更新
发布3.0版本

- 支持后台管理功能,后台管理代码仓库地址:https://gitee.com/bluexsx/box-im-admin
- 组件框架版本升级至: jdk17、springboot3.3、node18
- 部分ui，功能、性能优化


#### 在线体验

账号：张三/123456 李四/123456,也可以在网页端自行注册账号

网页端：https://www.boxim.online

移动安卓端：https://www.boxim.online/download/boxim.apk

移动H5端: https://www.boxim.online/h5/ ,或扫码：

![输入图片说明](%E6%88%AA%E5%9B%BE/h5%E4%BA%8C%E7%BB%B4%E7%A0%81.png)

由于微信小程序每次发布审核过于严苛和繁琐，暂时不再提供体验环境，但uniapp端依然会继续兼容小程序



#### 项目结构
|  模块  | 功能                               |
|-------------|----------------------------------|
| im-platform | 业务平台服务，负责处理来自用户的业务请求(http)       |
| im-server   | 消息推送服务，不依赖业务,负责将消息推送给用户(ws)      |
| im-client   | 消息推送sdk, 任何服务均可集成此sdk与im-server通信 |
| im-common   | 公共包                              |
| im-ui       | web页面                            |
| im-uniapp   | uniapp页面                         |

#### 消息推送方案
![输入图片说明](%E6%88%AA%E5%9B%BE/%E6%B6%88%E6%81%AF%E6%8E%A8%E9%80%81%E9%9B%86%E7%BE%A4%E5%8C%96.jpg)

- 当消息的发送者和接收者连的不是同一个server时，消息是无法直接推送的，所以我们需要设计出能够支持跨节点推送的方案
- 利用了redis的list数据实现消息推送，其中key为im:unread:${serverid},每个key的数据可以看做一个queue,每个im-server根据自身的id只消费属于自己的queue
- redis记录了每个用户的websocket连接的是哪个im-server,当用户发送消息时，im-platform将根据所连接的im-server的id,决定将消息推向哪个queue


#### 本地快速部署
1.安装运行环境
- 安装node:v18.19.0
- 安装jdk:17
- 安装maven:3.9.6
- 安装mysql:8.0,账号密码分别为root/root,运行sql脚本(脚本在im-platfrom的resources/db目录)
- 安装redis:6.2
- 安装minio:RELEASE.2024-xx,使用默认账号、密码、端口

2.启动后端服务
```
mvn clean package
java -jar ./im-platform/target/im-platform.jar
java -jar ./im-server/target/im-server.jar
```

3.启动前端web
```
cd im-web
npm install
npm run serve
```
访问 http://localhost:8080

4.启动uniapp-h5
将im-uniapp目录导入HBuilderX,点击菜单"运行"->"开发环境-h5"
访问 http://localhost:5173


#### 界面截图
私聊：

群聊：
![输入图片说明](%E6%88%AA%E5%9B%BE/web/%E7%BE%A4%E8%81%8A1.jpg)

![输入图片说明](%E6%88%AA%E5%9B%BE/web/%E7%BE%A4%E8%81%8A2.jpg)


好友列表：
![输入图片说明](%E6%88%AA%E5%9B%BE/web/%E5%A5%BD%E5%8F%8B%E5%88%97%E8%A1%A8.jpg)

群聊列表：
![输入图片说明](%E6%88%AA%E5%9B%BE/web/%E7%BE%A4%E8%81%8A%E5%88%97%E8%A1%A8.jpg)

微信小程序:
![输入图片说明](%E6%88%AA%E5%9B%BE/wx-mp/%E8%81%8A%E5%A4%A9.jpg)

![输入图片说明](%E6%88%AA%E5%9B%BE/wx-mp/%E5%85%B6%E4%BB%96.jpg)

#### 加入交流群
1群目前已满员，扫码进入2群:

![输入图片说明](%E6%88%AA%E5%9B%BE/%E4%BA%A4%E6%B5%81%E7%BE%A42.png)

欢迎进群与小伙们一起交流， **申请加群前请务必先star哦** 

#### 点下star吧
如果项目对您有帮助，请点亮右上方的star，支持一下作者吧！

#### 说明几点

1. 本系统允许用于商业用途，且不收费（自愿投币）。**但切记不要用于任何非法用途** ，本软件作者不会为此承担任何责任
1. 基于本系统二次开发后再次开源的项目，请注明引用出处，以避免引发不必要的误会
1. 作者目前不打算接项目，如果能接受1k/天以上的报价，也可以聊聊

