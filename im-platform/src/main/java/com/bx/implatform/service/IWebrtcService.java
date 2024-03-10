package com.bx.implatform.service;

import com.bx.implatform.config.ICEServer;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * webrtc 通信服务
 *
 * @author
 */
public interface IWebrtcService {

    void call(Long uid, String mode,String offer);

    void accept(Long uid,  String answer);

    void reject(Long uid);

    void cancel(Long uid);

    void failed(Long uid, String reason);

    void leave(Long uid);

    void candidate(Long uid, String candidate);

    List<ICEServer> getIceServers();


}
