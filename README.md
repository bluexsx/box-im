
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
1. 服务器支持集群化部署,具有良好的横向扩展能力


详细文档：https://www.yuque.com/u1475064/mufu2a


#### 近期更新
发布3.0版本：

- 后台管理端上线,后台管理代码仓库地址:https://gitee.com/bluexsx/box-im-admin
- 框架和组件版本全面升级: jdk17、springboot3.3、node18等
- 部分界面，功能、性能优化


#### 在线体验

网页端：https://www.boxim.online

移动安卓端：https://www.boxim.online/download/boxim.apk

移动ios端: 已上架至app store,搜索"盒子IM",下载安装即可

移动H5端: https://www.boxim.online/h5/ ,或扫码：

![输入图片说明](%E6%88%AA%E5%9B%BE/h5%E4%BA%8C%E7%BB%B4%E7%A0%81.png)

测试账号：张三/Aa123123 李四/Aa123123,也可以自行注册

说明:  
1.**请勿利用测试账号辱骂他人、发布低俗内容，否则将直接对您的IP进行封禁**  
2.由于微信小程序每次发布审核过于严苛和繁琐，暂时不再提供体验环境，但uniapp端依然会继续兼容小程序  
3.体验环境部署的是商业版本,与开源版本功能存在一定差异，具体请参考:  
https://www.yuque.com/u1475064/imk5n2/qtezcg32q1d0dr29#SbvXq


#### 付费服务
商业版源码: https://www.yuque.com/u1475064/imk5n2/qtezcg32q1d0dr29  
远程协助: https://www.yuque.com/u1475064/imk5n2/fettd57rvzc29s5r  
环境搭建: https://www.yuque.com/u1475064/imk5n2/qgq5cvgmavallqnl


#### 项目结构
| 模块          | 功能                               |
|-------------|----------------------------------|
| im-platform | 业务平台服务，负责处理来自用户的业务请求(http)       |
| im-server   | 消息推送服务，不依赖业务,负责将消息推送给用户(ws)      |
| im-client   | 消息推送sdk, 其他服务可集成此sdk与im-server通信 |
| im-common   | 公共包,后端服务均依赖此包                    |
| im-web      | web页面                            |
| im-uniapp   | uniapp页面,可打包成app、h5、微信小程序        |

#### 消息推送方案
当消息的发送者和接收者连的不是同一个server时，消息是无法直接推送的，所以我们设计出了能够支持跨节点推送的方案：
![输入图片说明](%E6%88%AA%E5%9B%BE/%E6%B6%88%E6%81%AF%E6%8E%A8%E9%80%81%E9%9B%86%E7%BE%A4%E5%8C%96.jpg)

- 利用了redis的list数据实现消息推送，其中key为im:unread:${serverid},每个key的数据可以看做一个queue,每个im-server根据自身的id只消费属于自己的queue
- redis记录了每个用户的websocket连接的是哪个im-server,当用户发送消息时，im-platform将根据所连接的im-server的id,决定将消息推向哪个queue


#### 本地启动
1.安装运行环境
- 安装node:v18.19.0
- 安装jdk:17
- 安装maven:3.9.6
- 安装mysql:8.0,账号密码分别为root/root,创建名为im_platform的数据库，运行db/im_platfrom.sql脚本
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

#### 接入消息推送
盒子IM对消息推送模块进行了剥离和封装， 如果您不关心盒子IM的业务功能，只需要一个可以将消息推送至前端的服务，则只需要启动im-server即可
然后参考以下文档进行接入:   
https://www.yuque.com/u1475064/mufu2a/vn5u10ephxh9sau8

#### 界面截图
私聊：
![输入图片说明](%E6%88%AA%E5%9B%BE/web/%E7%A7%81%E8%81%8A.jpg)

群聊：
![输入图片说明](%E6%88%AA%E5%9B%BE/web/%E7%BE%A4%E8%81%8A.jpg)

群通话：
![输入图片说明](%E6%88%AA%E5%9B%BE/web/%E5%A4%9A%E4%BA%BA%E9%80%9A%E8%AF%9D.jpg)

好友列表：
![输入图片说明](%E6%88%AA%E5%9B%BE/web/%E5%A5%BD%E5%8F%8B.jpg)

群列表：
![输入图片说明](%E6%88%AA%E5%9B%BE/web/%E7%BE%A4%E5%88%97%E8%A1%A8.jpg)

移动端APP:
![输入图片说明](%E6%88%AA%E5%9B%BE/app/1.png)  
  

![输入图片说明](%E6%88%AA%E5%9B%BE/app/2.png)  

#### 加入交流群
群1: 741174521(已满)  
群2: 937470451(已满)  
群3: 1012017031(已满）  
群4: 765849133

欢迎进群与小伙们一起交流， **申请加群前请务必先star哦** 


#### 点下star吧
如果项目对您有帮助，请点亮右上方的star，支持一下作者吧！


#### 说明几点

1. 本系统允许用于商业用途，且不收费，**但切记不要用于任何非法用途** ，本软件作者不会为此承担任何责任
1. 基于本系统二次开发后再次开源的项目，请注明引用出处，以避免引发不必要的误会
1. 为方便管理，要pr的同学请将代码提交到v_3.0.0分支，作者会在功能上线时合并到master分支
1. 允许商用不代表可以随意倒卖开源代码，尤其是代码、名字、截图一点都不改就拿去卖钱的，我劝你们善良一点！

