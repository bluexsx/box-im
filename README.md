
#### 项目介绍
1. 盒子IM是一个仿微信实现的网页版聊天软件，目前完全开源，仅用于学习和交流。
1. 支持私聊、群聊、离线消息、发送图片、文件、好友在线状态显示等功能。
1. 后端采用springboot+netty实现，前端使用vue。
1. 服务器支持集群化部署，每个im-server仅处理自身连接用户的消息


#### 近期更新
1. 支持发送emoji表情
1. 支持发送语音消息
1. 支持撤回和删除消息
1. 支持视频聊天(基于webrtc实现,需要ssl证书)


#### 在线体验
体验地址：https://www.boxim.online

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
| im-client   | 消息推送sdk|
| im-common   | 公共包  |


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
#### 快速接入
消息推送的请求代码已经封装在im-client包中，对于需要接入im-server的小伙伴，可以按照下面的教程快速的将IM功能集成到自己的项目中。

注意服务器端和网页端都需要接入，服务器端发送消息，网页端接收消息。

4.1 服务器端接入

引入pom文件
```
<dependency>
    <groupId>com.bx</groupId>
    <artifactId>im-client</artifactId>
    <version>1.1.0</version>
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
     * @param recvId 接收用户id
     * @param messageInfo 消息体，将转成json发送到客户端
     */
    void sendPrivateMessage(Long recvId, PrivateMessageInfo... messageInfo)；
     

    /**
     * 发送群聊消息
     *
     * @param recvIds 群聊用户id列表
     * @param messageInfo 消息体，将转成json发送到客户端
     */
    void sendGroupMessage(List<Long> recvIds, GroupMessageInfo... messageInfo)；
      
}
```

发送私聊消息(群聊也是类似的方式)：
```
 @Autowired
 private IMClient imClient;

 public void sendMessage(){
    PrivateMessageInfo messageInfo = new PrivateMessageInfo();
    Long recvId = 1L;
    messageInfo.setId(123L);
    messageInfo.setContent("你好呀");
    messageInfo.setType(MessageType.TEXT.getCode());
    messageInfo.setSendId(userId);
    messageInfo.setRecvId(recvId);
    messageInfo.setSendTime(new Date());
    imClient.sendPrivateMessage(recvId,messageInfo);
}

```

如果需要对消息发送的结果进行监听的话，实现MessageListener,并加上@IMListener即可
```
@Slf4j
@IMListener(type = IMListenerType.ALL)
public class PrivateMessageListener implements MessageListener {
    
    @Override
    public void process(SendResult result){
        PrivateMessageInfo messageInfo = (PrivateMessageInfo) result.getMessageInfo();
        if(result.getStatus().equals(IMSendStatus.SUCCESS)){
            // 消息发送成功
            log.info("消息已读，消息id:{}，发送者:{},接收者:{}",messageInfo.getId(),messageInfo.getSendId(),messageInfo.getRecvId());
        }
    }

}
```

4.2 网页端接入
首先将im-ui/src/api/wssocket.js拷贝到自己的项目。

接入代码如下：
```
import * as wsApi from './api/wssocket';

let wsUrl = 'ws://localhost:8878/im'
let userId = 1;
wsApi.createWebSocket(wsUrl , userId);
wsApi.onopen(() => {
    // 连接打开
    console.log("连接成功");
});
wsApi.onmessage((cmd,messageInfo) => {
    if (cmd == 2) {
    	// 异地登录，强制下线
    	console.log("您已在其他地方登陆，将被强制下线");
    } else if (cmd == 3) {
    	// 私聊消息
    	console.log(messageInfo);
    } else if (cmd == 4) {
    	// 群聊消息
    	console.log(messageInfo);
    }

})
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


#### 联系方式

![输入图片说明](%E6%88%AA%E5%9B%BE/%E7%BE%A4%E8%81%8A.jpg)

有任何问题，欢迎加群交流~


#### 点下star吧
喜欢的朋友麻烦点个star，鼓励一下作者吧！

