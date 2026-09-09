# 盒子IM

![MIT协议](https://img.shields.io/badge/license-MIT-red)
[![star](https://gitee.com/bluexsx/box-im/badge/star.svg)](https://gitee.com/bluexsx/box-im)
[![star](https://img.shields.io/github/stars/bluexsx/box-im.svg?style=flat&logo=GitHub)](https://github.com/bluexsx/box-im)
<a href="#加入交流群"><img src="https://img.shields.io/badge/交流群-green.svg?style=plasticr"></a>

盒子IM 是一个类似微信效果的即时通讯系统，不依赖任何第三方收费组件。

- 支持私聊、群聊、离线消息、语音 / 图片 / 文件、已读未读、群 @ 等功能
- 支持音视频通话，基于原生 WebRTC 实现，不依赖收费第三方 SDK
- uniapp 端兼容 App、H5、微信小程序，可与 Web 端同时在线，支持消息同步
- 后端采用 Spring Boot + Netty，网页端用 Vue，移动端用 uniapp
- 服务器支持集群化部署，具备良好的横向扩展能力

技术文档：https://www.yuque.com/u1475064/mufu2a

## 管理后台

管理后台为**独立仓库**，提供用户、群组、消息、敏感词等后台能力。建议先完成本仓库业务服务的启动，再按后台仓库文档搭建管理端。

- Gitee：https://gitee.com/bluexsx/box-im-admin
- GitHub：https://github.com/bluexsx/box-im-admin

## 近期重大更新

- 网页端技术框架升级至 Vue3 + Vite + TS
- 正式兼容纯血华为鸿蒙 App
- 本地消息重构，采用 IndexedDB / SQLite 存储本地消息

## 在线体验

| 终端     | 说明                                                                                                   |
|--------|------------------------------------------------------------------------------------------------------|
| Web    | https://www.boximchat.com                                                                            |
| 安卓 App | https://www.boximchat.com/download/boxim.apk                                                         |
| iOS App | 已上架 App Store，搜索「盒子IM」即可下载                                                                           |
| 微信小程序  | 已上架，搜索「盒子IM」即可进入                                                                                     |
| H5     | https://www.boximchat.com/h5/ <br> ![H5二维码](%E6%88%AA%E5%9B%BE/h5%E4%BA%8C%E7%BB%B4%E7%A0%81.png) |

**体验账号：** 张三 / Aa888999 、李四 / Aa888999

**说明：**

1. **请勿利用测试账号辱骂他人、发布低俗内容**
2. 为防止有人利用演示环境进行非法用途，演示环境注册通道已关闭
3. 演示环境部署的是商业版本，与开源版本功能存在一定差异，具体请参考：  
   https://www.yuque.com/u1475064/imk5n2/qtezcg32q1d0dr29#SbvXq

## 性能测试

- 压测模拟器：https://gitee.com/bluexsx/box-im-simulator
- 性能测试文档：https://www.yuque.com/u1475064/mufu2a/yhcxi9i6yqbkmm54

## 付费服务

- 商业版：https://www.yuque.com/u1475064/imk5n2/qtezcg32q1d0dr29
- 代部署：https://www.yuque.com/u1475064/imk5n2/qgq5cvgmavallqnl

## 项目结构

| 模块 | 功能 |
|------|------|
| im-platform | 业务平台服务，负责处理来自用户的业务请求（HTTP） |
| im-server | 消息推送服务，不依赖业务，负责将消息推送给用户（WS） |
| im-client | 消息推送 SDK，其他服务可集成此 SDK 与 im-server 通信 |
| im-common | 公共包，后端服务均依赖此包 |
| im-web | Web 页面 |
| im-uniapp | uniapp 页面，可打包成 App、H5、微信小程序 |

## 消息推送方案

当消息的发送者和接收者连的不是同一个 server 时，消息无法直接推送，因此设计了支持跨节点推送的方案：

![消息推送集群化](%E6%88%AA%E5%9B%BE/%E6%B6%88%E6%81%AF%E6%8E%A8%E9%80%81%E9%9B%86%E7%BE%A4%E5%8C%96.jpg)

- 利用 Redis 的 List 实现消息推送，其中 key 为 `im:message:private:${serverId}`，每个 key 可视为一个 queue，每个 im-server 根据自身 id 只消费属于自己的 queue
- Redis 记录了每个用户的 WS 连接所在的 im-server；用户发送消息时，im-platform 根据其所连接的 im-server id，决定将消息推向哪个 queue

## 本地启动

### 1. 安装运行环境

- Node：v18.19.0
- JDK：17
- Maven：3.9.6
- MySQL：8.0（账号密码均为 `root` / `root`），创建名为 `im_platform` 的数据库，并执行 `db/im_platfrom.sql` 脚本
- Redis：6.2
- MinIO：RELEASE.2024-xx，使用默认账号、密码、端口

### 2. 启动后端服务

```bash
mvn clean package
java -jar ./im-platform/target/im-platform.jar
java -jar ./im-server/target/im-server.jar
```

### 3. 启动前端 Web

```bash
cd im-web
npm install
npm run serve
```

访问 http://localhost:8080

### 4. 启动 uniapp H5

将 `im-uniapp` 目录导入 HBuilderX，点击菜单「运行」→「开发环境-h5」

访问 http://localhost:5173

## 界面截图

**私聊：**

![私聊](%E6%88%AA%E5%9B%BE/web/%E7%A7%81%E8%81%8A.jpg)

**群聊：**

![群聊](%E6%88%AA%E5%9B%BE/web/%E7%BE%A4%E8%81%8A.jpg)

**群通话（商业版）：**

![群通话](%E6%88%AA%E5%9B%BE/web/%E5%A4%9A%E4%BA%BA%E9%80%9A%E8%AF%9D.jpg)

**好友列表：**

![好友列表](%E6%88%AA%E5%9B%BE/web/%E5%A5%BD%E5%8F%8B.jpg)

**群列表：**

![群列表](%E6%88%AA%E5%9B%BE/web/%E7%BE%A4%E5%88%97%E8%A1%A8.jpg)

**移动端 App：**

![移动端App1](%E6%88%AA%E5%9B%BE/app/1.png)

![移动端App2](%E6%88%AA%E5%9B%BE/app/2.png)

## 加入交流群

从 2026-01-01 开始，我们正式开通了企业微信群（原来的 QQ 群不再开放）：

![交流群二维码](%E6%88%AA%E5%9B%BE/%E7%BE%A4%E4%BA%8C%E7%BB%B4%E7%A0%81.png)

欢迎进群与小伙们一起交流，**申请加群前请务必先 star 哦**

## 点下 star 吧

如果项目对您有帮助，请点亮右上方的 star，支持一下作者吧！

## 说明几点

1. 开源版允许用于商业用途，且不收费，**但切记不要用于任何非法用途**，本软件作者不会为此承担任何责任
2. 基于本系统二次开发后再次开源的项目，请注明引用出处，以避免引发不必要的误会
3. 为方便管理，要 PR 的同学请将代码提交到 `v_4.0.0` 分支，作者会在功能上线时合并到 `master` 分支
