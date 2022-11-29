package com.bx.implatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix="webrtc")
public class ICEServerConfig {

    private List<ICEServer> iceServers = new ArrayList<>();

}
