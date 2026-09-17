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


## 🌐 Web Resources & Aesthetic Symbols Index
- [SYM 267C](https://occult-aesthetic-symbols-26.pages.dev/symbol/sym-267c/)
- [SYM 1D41C](https://gothic-bio-fonts-98.pages.dev/symbol/sym-1d41c/)
- [SYM 2732](https://baroque-unicode-decor-43.pages.dev/symbol/sym-2732/)
- [SYM 1F972](https://angelic-soft-text-59.pages.dev/symbol/sym-1f972/)
- [SYM 1F608](https://cyber-clan-tags-55.pages.dev/symbol/sym-1f608/)
- [SYM 26CD](https://sleek-unicode-art-69.pages.dev/symbol/sym-26cd/)
- [OPEN CENTRE STAR](https://sleek-type-aesthetic-51.pages.dev/symbol/open-centre-star/)
- [SYM 1F60E](https://baroque-unicode-decor-43.pages.dev/symbol/sym-1f60e/)
- [SYM 26AB](https://subtle-sparkle-text-86.pages.dev/symbol/sym-26ab/)
- [SYM 2733](https://sleek-type-aesthetic-51.pages.dev/symbol/sym-2733/)
- [SYM 1D455](https://angelic-soft-text-59.pages.dev/symbol/sym-1d455/)
- [SYM 1F633](https://pink-bow-fonts-37.pages.dev/symbol/sym-1f633/)
- [SYM 1F627](https://scholarly-script-hub-43.pages.dev/symbol/sym-1f627/)
- [SYM 26C0](https://manga-speech-symbols-95.pages.dev/symbol/sym-26c0/)
- [TRENDING](https://sleek-unicode-art-69.pages.dev/ru/trending/)
- [BEAMED SIXTEENTH MUSICAL NOTES](https://ballet-core-symbols-11.pages.dev/symbol/beamed-sixteenth-musical-notes/)
- [BRACKETS](https://sleek-unicode-art-69.pages.dev/es/brackets/)
- [SYM 1F621](https://angelic-soft-text-59.pages.dev/symbol/sym-1f621/)
- [DAGGER CROSS SYMBOL](https://vintage-lace-symbols-65.pages.dev/symbol/dagger-cross-symbol/)
- [SYM 26A6](https://baroque-unicode-decor-43.pages.dev/symbol/sym-26a6/)
- [GAMING WEAPONS](https://manga-speech-symbols-95.pages.dev/vi/gaming-weapons/)
- [SYM 1F60E](https://vintage-runes-text-63.pages.dev/symbol/sym-1f60e/)
- [SYM 1D433](https://gothic-bio-fonts-98.pages.dev/symbol/sym-1d433/)
- [SYM 2685](https://kawaii-kaomoji-hub-31.pages.dev/symbol/sym-2685/)
- [SYM 1F632](https://cyber-clan-tags-20.pages.dev/symbol/sym-1f632/)
- [SYM 265F](https://vintage-lace-symbols-65.pages.dev/symbol/sym-265f/)
- [SYM 1D428](https://scholarly-script-hub-43.pages.dev/symbol/sym-1d428/)
- [SYM 1D41F](https://scholarly-script-hub-43.pages.dev/symbol/sym-1d41f/)
- [SYM 1D44B](https://classic-literature-symbols-64.pages.dev/symbol/sym-1d44b/)
- [SYM 26EF](https://coquette-aesthetic-symbols-51.pages.dev/symbol/sym-26ef/)
- [SYM 1D421](https://gothic-bio-fonts-98.pages.dev/symbol/sym-1d421/)
- [SYM 26DB](https://angelic-soft-text-59.pages.dev/symbol/sym-26db/)
- [SYM 265A](https://scholarly-script-hub-43.pages.dev/symbol/sym-265a/)
- [KAOMOJI](https://sleek-unicode-art-69.pages.dev/pt/kaomoji/)
- [SYM 1D468](https://subtle-sparkle-text-86.pages.dev/symbol/sym-1d468/)
- [SYM 1F63A](https://kawaii-kaomoji-hub-31.pages.dev/symbol/sym-1f63a/)
- [SYM 273B](https://classic-literature-symbols-64.pages.dev/symbol/sym-273b/)
- [ROBLOX NAMES](https://pastel-moe-kaomoji-91.pages.dev/es/roblox-names/)
- [SYM 1D4A1](https://minimal-star-symbols-22.pages.dev/symbol/sym-1d4a1/)
- [SYM 274A](https://ballet-core-symbols-11.pages.dev/symbol/sym-274a/)
- [UPWARD DIAGONAL ARROW](https://pink-bow-fonts-37.pages.dev/symbol/upward-diagonal-arrow/)
- [ZODIAC CELESTIAL](https://clean-line-emojis-77.pages.dev/vi/zodiac-celestial/)
- [SYM 260B](https://anime-sparkle-text-45.pages.dev/symbol/sym-260b/)
- [TENDER GENTLE TEAR KAOMOJI](https://pink-ribbon-fonts-28.pages.dev/symbol/tender-gentle-tear-kaomoji/)
- [BRACKETS](https://soft-angel-unicode-43.pages.dev/ru/brackets/)
- [SYM 1F640](https://anime-sparkle-text-45.pages.dev/symbol/sym-1f640/)
- [SYM 1F929](https://clean-line-emojis-77.pages.dev/symbol/sym-1f929/)
- [STAR OPERATOR](https://anime-sparkle-text-45.pages.dev/symbol/star-operator/)
- [TRENDING](https://sleek-unicode-art-69.pages.dev/pt/trending/)
- [SYM 26DC](https://sleek-unicode-art-69.pages.dev/symbol/sym-26dc/)
- [SYM 1D474](https://chibi-emoticon-world-87.pages.dev/symbol/sym-1d474/)
- [SYM 1F915](https://sleek-type-aesthetic-51.pages.dev/symbol/sym-1f915/)
- [SYM 1F644](https://clean-line-emojis-77.pages.dev/symbol/sym-1f644/)
- [SYM 268E](https://clean-line-emojis-77.pages.dev/symbol/sym-268e/)
- [SYM 1F640](https://chibi-emoticon-world-87.pages.dev/symbol/sym-1f640/)
- [SYM 1D479](https://soft-angel-unicode-43.pages.dev/symbol/sym-1d479/)
- [CHEERING FIGHTING FIST KAOMOJI](https://ballet-core-symbols-11.pages.dev/symbol/cheering-fighting-fist-kaomoji/)
- [SYM 1F979](https://anime-sparkle-text-45.pages.dev/symbol/sym-1f979/)
- [INSTAGRAM BIO](https://sleek-unicode-art-69.pages.dev/pt/instagram-bio/)
- [SYM 1D47C](https://neon-futuristic-symbols-62.pages.dev/symbol/sym-1d47c/)
- [SYM 1D44F](https://scholarly-script-hub-43.pages.dev/symbol/sym-1d44f/)
- [LEFT MATHEMATICAL WHITE SQUARE BRACKET](https://vintage-runes-text-63.pages.dev/symbol/left-mathematical-white-square-bracket/)
- [SYM 2741](https://vintage-lace-symbols-65.pages.dev/symbol/sym-2741/)
- [SYM 26AD](https://synthwave-text-art-35.pages.dev/symbol/sym-26ad/)
- [SYM 1FAE3](https://neon-futuristic-symbols-62.pages.dev/symbol/sym-1fae3/)
- [LATIN CROSS HEAVY](https://clean-line-emojis-77.pages.dev/symbol/latin-cross-heavy/)
- [SYM 265D](https://clean-line-emojis-77.pages.dev/symbol/sym-265d/)
- [SYM 2644](https://classic-literature-symbols-64.pages.dev/symbol/sym-2644/)
- [SYM 1D46F](https://chibi-emoticon-world-87.pages.dev/symbol/sym-1d46f/)
- [SYM 1D417](https://soft-angel-unicode-43.pages.dev/symbol/sym-1d417/)
- [SYM 1D401](https://gothic-bio-fonts-90.pages.dev/symbol/sym-1d401/)
- [SYM 2728](https://pastel-moe-kaomoji-91.pages.dev/symbol/sym-2728/)
- [SYM 2721](https://manga-speech-symbols-95.pages.dev/symbol/sym-2721/)
- [SYM 1F626](https://baroque-unicode-decor-43.pages.dev/symbol/sym-1f626/)
- [SYM 2640](https://clean-line-emojis-77.pages.dev/symbol/sym-2640/)
- [SYM 1D4A3](https://anime-sparkle-text-45.pages.dev/symbol/sym-1d4a3/)
- [SYM 1D408](https://scholarly-script-hub-43.pages.dev/symbol/sym-1d408/)
- [BLUSHING SOFT SMILE KAOMOJI](https://matrix-terminal-fonts-30.pages.dev/symbol/blushing-soft-smile-kaomoji/)
- [SYM 1D402](https://sleek-unicode-art-69.pages.dev/symbol/sym-1d402/)
- [SYM 1D45D](https://clean-line-emojis-77.pages.dev/symbol/sym-1d45d/)
- [SYM 2764 FE0F 200D 1FA79](https://angelic-soft-text-59.pages.dev/symbol/sym-2764-fe0f-200d-1fa79/)
- [SYM 2646](https://kawaii-kaomoji-hub-95.pages.dev/symbol/sym-2646/)
- [SYM 1F92C](https://ballet-core-symbols-11.pages.dev/symbol/sym-1f92c/)
- [SYM 1F910](https://subtle-sparkle-text-86.pages.dev/symbol/sym-1f910/)
- [KAOMOJI](https://zen-arrow-symbols-99.pages.dev/vi/kaomoji/)
- [SYM 2688](https://soft-angel-unicode-43.pages.dev/symbol/sym-2688/)
- [SYM 2647](https://matrix-terminal-fonts-30.pages.dev/symbol/sym-2647/)
- [SYM 2663](https://soft-angel-unicode-43.pages.dev/symbol/sym-2663/)
- [SYM 1F49F](https://cute-face-emoticons-66.pages.dev/symbol/sym-1f49f/)
- [SYM 26BB](https://soft-angel-unicode-43.pages.dev/symbol/sym-26bb/)
- [SYM 26AC](https://subtle-sparkle-text-86.pages.dev/symbol/sym-26ac/)
- [WATER BUBBLES](https://sleek-type-aesthetic-51.pages.dev/symbol/water-bubbles/)
- [SYM 1F627](https://cute-face-emoticons-66.pages.dev/symbol/sym-1f627/)
- [SYM 1D490](https://sleek-unicode-art-69.pages.dev/symbol/sym-1d490/)
- [SYM 1D430](https://chibi-emoticon-world-87.pages.dev/symbol/sym-1d430/)
- [GAMING WEAPONS](https://sleek-unicode-art-69.pages.dev/pt/gaming-weapons/)
- [SYM 1D498](https://anime-sparkle-text-45.pages.dev/symbol/sym-1d498/)
- [SYM 1F922](https://zen-arrow-symbols-99.pages.dev/symbol/sym-1f922/)
- [SYM 1D410](https://sleek-unicode-art-69.pages.dev/symbol/sym-1d410/)
- [SYM 1F62D](https://ballet-core-symbols-11.pages.dev/symbol/sym-1f62d/)
- [SYM 1D405](https://chibi-emoticon-world-87.pages.dev/symbol/sym-1d405/)
- [BRACKETS](https://pink-bow-fonts-37.pages.dev/vi/brackets/)
- [SYM 2671](https://soft-angel-unicode-43.pages.dev/symbol/sym-2671/)
- [SYM 1F642](https://sleek-type-aesthetic-51.pages.dev/symbol/sym-1f642/)
- [SYM 1F49E](https://clean-line-emojis-77.pages.dev/symbol/sym-1f49e/)
- [SYM 1F97A](https://chibi-emoticon-world-87.pages.dev/symbol/sym-1f97a/)
- [SYM 1D448](https://academic-rune-text-25.pages.dev/symbol/sym-1d448/)
- [INSTAGRAM BIO](https://vintage-runes-text-63.pages.dev/pt/instagram-bio/)
- [INSTAGRAM BIO](https://pink-bow-fonts-37.pages.dev/es/instagram-bio/)
- [SYM 26E6](https://clean-line-emojis-77.pages.dev/symbol/sym-26e6/)
- [PINWHEEL STAR](https://academic-rune-text-25.pages.dev/symbol/pinwheel-star/)
- [SYM 26C3](https://minimal-star-symbols-54.pages.dev/symbol/sym-26c3/)
- [ROBLOX NAMES](https://manga-speech-symbols-95.pages.dev/roblox-names/)
- [STARS](https://angelic-soft-text-59.pages.dev/pt/stars/)
- [SYM 1F47D](https://ballet-core-symbols-11.pages.dev/symbol/sym-1f47d/)
- [SYM 2655](https://sleek-unicode-art-69.pages.dev/symbol/sym-2655/)
- [SYM 1F62B](https://arcane-symbol-vault-32.pages.dev/symbol/sym-1f62b/)
- [SYM 1F61C](https://arcane-symbol-vault-32.pages.dev/symbol/sym-1f61c/)
- [SYM 1F600](https://ballet-core-symbols-11.pages.dev/symbol/sym-1f600/)
- [SYM 2680](https://baroque-unicode-decor-43.pages.dev/symbol/sym-2680/)
- [SYM 26B3](https://pink-ribbon-fonts-28.pages.dev/symbol/sym-26b3/)
- [SYM 1D430](https://coquette-aesthetic-symbols-51.pages.dev/symbol/sym-1d430/)
- [SYM 2738](https://coquette-aesthetic-symbols-51.pages.dev/symbol/sym-2738/)
- [SYM 1F493](https://anime-sparkle-text-45.pages.dev/symbol/sym-1f493/)
- [CLOCKWISE OPEN CIRCLE ARROW](https://coquette-aesthetic-symbols-51.pages.dev/symbol/clockwise-open-circle-arrow/)
- [SYM 1F920](https://coquette-aesthetic-symbols-51.pages.dev/symbol/sym-1f920/)
- [HEARTS](https://vintage-lace-symbols-65.pages.dev/pt/hearts/)
- [MUSIC WEATHER](https://anime-sparkle-text-45.pages.dev/es/music-weather/)
- [SYM 2686](https://angelic-soft-text-59.pages.dev/symbol/sym-2686/)
- [SYM 1D431](https://chibi-emoticon-world-87.pages.dev/symbol/sym-1d431/)
