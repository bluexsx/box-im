# docker 构建和部署指南

## 配置

请配置以下文件的挂载目录、端口号、环境变量、账户信息等。

- box-im/im-docker-compose/im-coturn/docker-compose.yml
- box-im/im-docker-compose/im-service/docker-compose.yml
- box-im/im-docker-compose/im-coturn/im-platform.env
- box-im/im-docker-compose/im-coturn/im-server.env

## 构建

可以在部署的目标服务器上在下面两个目录下构建出 im-server 和 im-platform 的 docker images.

- box-im/im-server/Dockerfile
- box-im/im-platform/Dockerfile

## 部署

在部署的目标服务器上通过 docker-compose 启动所有容器，

```bash
docker-compose -f im-docker-compose/im-coturn/docker-compose.yml up -d
docker-compose -f im-docker-compose/im-service/docker-compose.yml up -d
```
