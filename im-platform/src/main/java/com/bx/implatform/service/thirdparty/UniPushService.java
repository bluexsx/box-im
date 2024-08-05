package com.bx.implatform.service.thirdparty;

import com.bx.implatform.session.NotifySession;
import com.getui.push.v2.sdk.api.PushApi;
import com.getui.push.v2.sdk.common.ApiResult;
import com.getui.push.v2.sdk.dto.req.Audience;
import com.getui.push.v2.sdk.dto.req.Settings;
import com.getui.push.v2.sdk.dto.req.message.PushChannel;
import com.getui.push.v2.sdk.dto.req.message.PushDTO;
import com.getui.push.v2.sdk.dto.req.message.PushMessage;
import com.getui.push.v2.sdk.dto.req.message.android.AndroidDTO;
import com.getui.push.v2.sdk.dto.req.message.android.GTNotification;
import com.getui.push.v2.sdk.dto.req.message.android.ThirdNotification;
import com.getui.push.v2.sdk.dto.req.message.android.Ups;
import com.getui.push.v2.sdk.dto.req.message.ios.Alert;
import com.getui.push.v2.sdk.dto.req.message.ios.Aps;
import com.getui.push.v2.sdk.dto.req.message.ios.IosDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: 谢绍许
 * @date: 2024-07-06
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UniPushService {

    private final PushApi pushApi;

    public void pushByCid(NotifySession session, String body){
        //根据cid进行单推
        PushDTO<Audience> pushDTO = new PushDTO<Audience>();
        // 设置推送参数，requestid需要每次变化唯一
        pushDTO.setRequestId(System.currentTimeMillis()+"");
        Settings settings = new Settings();
        pushDTO.setSettings(settings);
        //消息有效期，走厂商消息必须设置该值
        settings.setTtl(3600000);
        //在线走个推通道时推送的消息体
        PushMessage pushMessage = new PushMessage();
        pushDTO.setPushMessage(pushMessage);
        //此格式的透传消息由 unipush 做了特殊处理，会自动展示通知栏。开发者也可自定义其它格式，在客户端自己处理。
        GTNotification gtNotification = new GTNotification();
        gtNotification.setTitle(session.getTitle());
        gtNotification.setBody(body);
        gtNotification.setClickType("startapp");
        gtNotification.setNotifyId(session.getNotifyId().toString());
        gtNotification.setLogoUrl(session.getLogo());
        gtNotification.setBadgeAddNum("1");
        pushMessage.setNotification(gtNotification);
        // 设置接收人信息
        Audience audience = new Audience();
        pushDTO.setAudience(audience);
        audience.addCid(session.getCid());
        //设置离线推送时的消息体
        PushChannel pushChannel = new PushChannel();
        //安卓离线厂商通道推送的消息体
        AndroidDTO androidDTO = new AndroidDTO();
        Ups ups = new Ups();
        ThirdNotification thirdNotification = new ThirdNotification();
        ups.setNotification(thirdNotification);
        ups.setOptions(buildOptions(session.getLogo()));
        thirdNotification.setTitle(session.getTitle());
        thirdNotification.setBody(body);
        thirdNotification.setNotifyId(session.getNotifyId().toString());
        // 打开首页
        thirdNotification.setClickType("startapp");
        androidDTO.setUps(ups);
        pushChannel.setAndroid(androidDTO);
        // ios离线apn通道推送的消息体
        Alert alert = new Alert();
        alert.setTitle(session.getTitle());
        alert.setBody(body);
        Aps aps = new Aps();
        // 0：普通通知消息  1:静默推送(无通知栏消息)，静默推送时不需要填写其他参数。苹果建议1小时最多推送3条静默消息
        aps.setContentAvailable(0);
        // default: 系统铃声  不填:无声
        aps.setSound("default");
        aps.setAlert(alert);

        IosDTO iosDTO = new IosDTO();
        iosDTO.setAps(aps);
        iosDTO.setType("notify");
        pushChannel.setIos(iosDTO);
        pushDTO.setPushChannel(pushChannel);
        // 进行cid单推
        ApiResult<Map<String, Map<String, String>>> apiResult = pushApi.pushToSingleByCid(pushDTO);
        if (apiResult.isSuccess()) {
            log.info("推送成功,{}",apiResult.getData());
        } else {
            log.info("推送失败,code:{},msg:{}",apiResult.getCode(),apiResult.getMsg());
        }
    }


    private Map<String, Map<String, Object>> buildOptions(String logo){
        Map<String, Map<String, Object>> options = new HashMap<>();
        // 小米
        Map<String,Object> xm = new HashMap<>();
        xm.put("/extra.notification_style_type",1);
        xm.put("/extra.notification_large_icon_uri",logo);
        options.put("XM",xm);
        // 华为
        Map<String,Object> hw = new HashMap<>();
        hw.put("/message/android/notification/image",logo);
        hw.put("/message/android/notification/style",1);
        options.put("HW",hw);
        // 荣耀
        Map<String,Object> ho = new HashMap<>();
        hw.put("/android/notification/badge/addNum",1);
        hw.put("/android/notification/icon",logo);
        options.put("HW",hw);
        return options;
    }
}
