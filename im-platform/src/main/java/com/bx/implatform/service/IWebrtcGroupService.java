package com.bx.implatform.service;

import com.bx.implatform.dto.*;
import com.bx.implatform.vo.WebrtcGroupInfoVO;

public interface IWebrtcGroupService {

    /**
     * 发起通话
     */
    void setup(WebrtcGroupSetupDTO dto);

    /**
     * 接受通话
     */
    void accept(Long groupId);

    /**
     * 拒绝通话
     */
    void reject(Long groupId);

    /**
     * 通话失败,如设备不支持、用户忙等(此接口为系统自动调用,无需用户操作，所以不抛异常)
     */
    void failed(WebrtcGroupFailedDTO dto);

    /**
     * 主动加入通话
     */
    void join(Long groupId);

    /**
     * 通话过程中继续邀请用户加入通话
     */
    void invite(WebrtcGroupInviteDTO dto);

    /**
     * 取消通话,仅通话发起人可以取消通话
     */
    void cancel(Long groupId);

    /**
     * 退出通话，如果当前没有人在通话中，将取消整个通话
     */
    void quit(Long groupId);

    /**
     * 推送offer信息给对方
     */
    void offer(WebrtcGroupOfferDTO dto);

    /**
     * 推送answer信息给对方
     */
    void answer(WebrtcGroupAnswerDTO dto);

    /**
     * 推送candidate信息给对方
     */
    void candidate(WebrtcGroupCandidateDTO dto);

    /**
     * 用户进行了设备操作，如果关闭摄像头
     */
    void device(WebrtcGroupDeviceDTO dto);

    /**
     * 查询通话信息
     */
    WebrtcGroupInfoVO info(Long groupId);

    /**
     * 心跳保持, 用户每15s上传一次心跳
     */
    void heartbeat(Long groupId);

}
