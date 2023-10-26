

#### 项目介绍
1. 盒子IM是一个仿微信实现的网页版聊天软件，目前完全开源，仅用于学习和交流
1. 支持私聊、群聊、离线消息、发送语音、图片、文件、emoji表情等功能
1. 支持视频聊天(基于webrtc实现,需要ssl证书)
1. 后端采用springboot+netty实现，前端使用vue
1. 服务器支持集群化部署，每个im-server仅处理自身连接用户的消息


#### 近期更新
发布2.0版本，本次更新主要是加入了uniapp版本：

- 支持移动端和web端同时在线，多端消息同步
- 目前仅兼容h5和微信小程序，后续会继续兼容更多终端类型

感兴趣的小伙伴，可在下方扫码体验



#### 在线体验
web地址：https://www.boxim.online

微信小程序：

![输入图片说明](%E6%88%AA%E5%9B%BE/wx%E5%B0%8F%E7%A8%8B%E5%BA%8F%E4%BA%8C%E7%BB%B4%E7%A0%81.jpg)

H5地址: https://www.boxim.online/h5/ ,或扫码：

![输入图片说明](%E6%88%AA%E5%9B%BE/h5%E4%BA%8C%E7%BB%B4%E7%A0%81.png)


账号：

张三/123456
李四/123456

也可以自行注册账号


#### 项目结构
|  模块  |     功能 |
|-------------|------------|
| im-platform | 与页面进行交互，处理业务请求 |
| im-server   | 推送聊天消息|
| im-client   | 消息推送sdk|
| im-common   | 公共包  |
| im-ui       | web页面  |
| im-uniapp   | app页面  |

#### 消息推送方案
![输入图片说明](%E6%88%AA%E5%9B%BE/%E6%B6%88%E6%81%AF%E6%8E%A8%E9%80%81%E9%9B%86%E7%BE%A4%E5%8C%96.jpg)

- 当消息的发送者和接收者连的不是同一个server时，消息是无法直接推送的，所以我们需要设计出能够支持跨节点推送的方案
- 利用了redis的list数据实现消息推送，其中key为im:unread:${serverid},每个key的数据可以看做一个queue,每个im-server根据自身的id只消费属于自己的queue
- redis记录了每个用户的websocket连接的是哪个im-server,当用户发送消息时，im-platform将根据所连接的im-server的id,决定将消息推向哪个queue


#### 本地快速部署
1.安装运行环境
- 安装node:v14.16.0
- 安装jdk:1.8
- 安装maven:3.6.3
- 安装mysql:5.7,密码分别为root/root,运行sql脚本(脚本在im-platfrom的resources/db目录)
- 安装redis:5.0
- 安装minio，命令端口使用9001，并创建一个名为"box-im"的bucket，并设置访问权限为公开

2.启动后端服务
```
mvn clean package
java -jar ./im-platform/target/im-platform.jar
java -jar ./im-server/target/im-server.jar
```

3.启动前端web
```
cd im-ui
npm install
npm run serve
```
访问 http://localhost:8080


4.启动uniapp-h5
将im-uniapp目录导入HBuilderX,点击菜单"运行"->"开发环境-h5"
访问 http://localhost:5173


#### 快速接入
消息推送的请求代码已经封装在im-client包中，对于需要接入im-server的小伙伴，可以按照下面的教程快速的将IM功能集成到自己的项目中。

注意服务器端和前端都需要接入，服务器端发送消息，前端接收消息。

4.1 服务器端接入

引入pom文件
```
<dependency>
    <groupId>com.bx</groupId>
    <artifactId>im-client</artifactId>
    <version>2.0.0</version>
</dependency>
```
内容使用了redis进行通信,所以要配置redis地址：

```
spring:
  redis:
    host: 127.0.0.1
    port: 6379
```

直接把IMClient通过@Autowire导进来就可以发送消息了，IMClient 只有2个接口：
```
public class IMClient {

    /**
     * 发送私聊消息
     *
     * @param message 私有消息
     */
    public<T> void sendPrivateMessage(IMPrivateMessage<T> message);

    /**
     * 发送群聊消息（发送结果通过MessageListener接收）
     *
     * @param message 群聊消息
     */
    public<T> void sendGroupMessage(IMGroupMessage<T> message);    
}
```

发送私聊消息(群聊也是类似的方式)：
```
 @Autowired
 private IMClient imClient;

 public void sendMessage(){
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        // 发送方的id和终端类型
        sendMessage.setSender(new IMUserInfo(1L, IMTerminalType.APP.code()));
        // 对方的id
        sendMessage.setRecvId(2L);
        // 推送给对方所有终端
        sendMessage.setRecvTerminals(IMTerminalType.codes());
        // 同时推送给自己的其他类型终端
        sendMessage.setSendToSelf(true);
        // 需要回推发送结果，将在IMListener接收发送结果
        sendMessage.setSendResult(true);
        // 推送的内容
        sendMessage.setData(msgInfo);
        // 推送消息
        imClient.sendPrivateMessage(sendMessage);
}

```
监听发送结果:
1.编写消息监听类，实现MessageListener,并加上@IMListener
2.发送消息时指定sendResult为true
```
@Slf4j
@IMListener(type = IMListenerType.ALL)
public class PrivateMessageListener implements MessageListener {
    
    @Override
    public void process(IMSendResult<PrivateMessageVO> result){
        PrivateMessageVO messageInfo = result.getData();
        if(result.getCode().equals(IMSendCode.SUCCESS.code())){
            log.info("消息发送成功，消息id:{}，发送者:{},接收者:{},终端:{}",messageInfo.getId(),result.getSender().getId(),result.getReceiver().getId(),result.getReceiver().getTerminal());
        }
    }
}
```

4.2 前端接入
首先将im-ui/src/api/wssocket.js拷贝到自己的项目。

接入代码如下：
```
import * as wsApi from './api/wssocket';

let wsUrl = 'ws://localhost:8878/im'
let token = "您的token";
wsApi.init(wsUrl,token);
wsApi.connect();
wsApi.onOpen(() => {
    // 连接打开
    console.log("连接成功");
});
wsApi.onMessage((cmd,msgInfo) => {
    if (cmd == 2) {
    	// 异地登录，强制下线
    	console.log("您已在其他地方登陆，将被强制下线");
    } else if (cmd == 3) {
    	// 私聊消息
    	console.log(msgInfo);
    } else if (cmd == 4) {
    	// 群聊消息
    	console.log(msgInfo);
    }
})
wsApi.onClose((e) => {
    console.log("连接关闭");
});
```


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

微信小程序:
![输入图片说明](%E6%88%AA%E5%9B%BE/%E5%BE%AE%E4%BF%A1%E5%B0%8F%E7%A8%8B%E5%BA%8F.jpg)

#### QQ交流群

![输入图片说明](%E6%88%AA%E5%9B%BE/%E4%BA%A4%E6%B5%81%E7%BE%A4.png)

欢迎进群与小伙们一起交流， **加群前请务必先star!!!** 


#### 嘿嘿
![输入图片说明](%E6%88%AA%E5%9B%BE/%E5%BE%AE%E4%BF%A1%E6%94%B6%E6%AC%BE%E7%A0%81.png)

悄悄放个二维码在这，宝子们..你懂我意思吧



#### 点下star吧
如果项目对您有帮助，请点亮右上方的star，支持一下作者吧！

<a href="https://github.com/bluexsx/box-im" target="_blank">
        <img alt="License" src="https://img.shields.io/github/stars/bluexsx/box-im.svg?style=social">
</a> 

[![star](https://gitee.com/bluexsx/box-im/badge/star.svg)](https://gitee.com/bluexsx/box-im) 