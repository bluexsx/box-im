package com.bx.imserver.netty;

import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.mq.RedisMQTemplate;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class IMServerGroup implements CommandLineRunner {

    public static volatile long serverId = 0;

    private final RedisMQTemplate redisMQTemplate;

    private final List<IMServer> imServers;

    /***
     * 判断服务器是否就绪
     *
     **/
    public boolean isReady() {
        for (IMServer imServer : imServers) {
            if (!imServer.isReady()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run(String... args) {
        // 初始化SERVER_ID
        String key = IMRedisKey.IM_MAX_SERVER_ID;
        serverId = redisMQTemplate.opsForValue().increment(key, 1);
        // 启动服务
        for (IMServer imServer : imServers) {
            imServer.start();
        }
    }

    @PreDestroy
    public void destroy() {
        // 停止服务
        for (IMServer imServer : imServers) {
            imServer.stop();
        }
    }
}
