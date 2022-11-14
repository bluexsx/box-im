
### 项目介绍

1. 盒子IM是一个仿微信实现的网页版聊天软件，目前完全开源，仅用于学习和交流。
1. 支持私聊、群聊、离线消息、发送图片、文件、好友在线状态显示等功能。
1. 后端采用springboot+netty实现，前端使用vue。
1. 服务器支持集群化部署，每个im-server仅处理自身连接用户的消息




#### 在线体验
体验地址：https://8.134.92.70

账号：
张三/123456
李四/123456
也可以自行注册账号

体验后记得帮忙点个star哟!




#### 项目结构
|  模块  |     功能 |
|-------------|------------|
| im-platform | 与页面进行交互，处理业务请求 |
| im-server   | 推送聊天消息|
| im-common   | 公共包  |


 #### 集群化方案
![输入图片说明](%E6%88%AA%E5%9B%BE/%E6%B6%88%E6%81%AF%E6%8E%A8%E9%80%81%E9%9B%86%E7%BE%A4%E5%8C%96.jpg)



- 当消息的发送者和接收者连的不是同一个server时，消息是无法直接推送的，所以我们需要设计出能够支持跨节点推送的方案
- 利用了redis的list类型数据实现消息推送，其中key为im:unread:${serverid},每个key的数据可以看做一个queue(或topic),每个im-server根据自己的serverid只消费属于自己的queue
- redis记录了每个用户的websocket连接的是哪个im-server,当用户发送消息时，im-platform将根据所连接的im-server的serverid,决定将消息推向哪个queue





#### 本地快速部署
1.安装运行环境
- 安装node:v14.16.0
- 安装jdk:1.8
- 安装maven:3.6.3
- 安装mysql:5.7,密码分别为root/root
- 安装redis:4.0
- 安装minio，命令端口使用9001，并创建一个名为"box-im"的bucket，并设置访问权限为公开




2.启动后端服务
```
mvn clean package
java -jar ./im-platform/target/im-platform.jar
java -jar ./im-server/target/im-server.jar
```

3.启动前端ui
```
cd im-ui
npm install
npm run serve
```

4.访问localhost:8080




#### 界面截图
文字聊天：
![输入图片说明](%E6%88%AA%E5%9B%BE/%E6%96%87%E5%AD%97%E8%81%8A%E5%A4%A9.jpg)

发送图片、文件：
![输入图片说明](%E6%88%AA%E5%9B%BE/%E5%8F%91%E9%80%81%E5%9B%BE%E7%89%87%E6%96%87%E4%BB%B6.jpg)

发送语音
![输入图片说明](%E6%88%AA%E5%9B%BE/%E5%8F%91%E9%80%81%E8%AF%AD%E9%9F%B3.jpg)

群聊：
![输入图片说明](%E6%88%AA%E5%9B%BE/%E7%BE%A4%E8%81%8A.jpg)

好友列表：
![输入图片说明](%E6%88%AA%E5%9B%BE/%E5%A5%BD%E5%8F%8B%E5%88%97%E8%A1%A8.jpg)

群聊列表：
![输入图片说明](%E6%88%AA%E5%9B%BE/%E7%BE%A4%E8%81%8A%E5%88%97%E8%A1%A8.jpg)





#### 联系方式
QQ: 825657193
邮箱：825657193@qq.com

有任何问题，欢迎给我留言哦


#### 最后
撸码不易，喜欢的朋友麻烦点个star吧！
