package com.bx.implatform.config;

import com.getui.push.v2.sdk.ApiHelper;
import com.getui.push.v2.sdk.GtApiConfiguration;
import com.getui.push.v2.sdk.api.PushApi;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author: 谢绍许
 * @date: 2024-07-06
 * @version: 1.0
 */
@Data
@Component
public class UniPushConfig {

    @Value("${notify.uniPush.appId}")
    private String appId;
    @Value("${notify.uniPush.appKey}")
    private String appKey;
    @Value("${notify.uniPush.masterSecret}")
    private String masterSecret;

    @Bean
    public GtApiConfiguration uniPushConfiguration(){
        GtApiConfiguration apiConfiguration = new GtApiConfiguration();
        apiConfiguration.setAppId(appId);
        apiConfiguration.setAppKey(appKey);
        apiConfiguration.setMasterSecret(masterSecret);
        return apiConfiguration;
    }

    @Bean
    public PushApi uniPushApi(GtApiConfiguration configuration){
        // 实例化ApiHelper对象，用于创建接口对象
        ApiHelper apiHelper = ApiHelper.build(configuration);
        // 创建对象，建议复用。目前有PushApi、StatisticApi、UserApi
        PushApi pushApi = apiHelper.creatApi(PushApi.class);
        return pushApi;
    }
}
