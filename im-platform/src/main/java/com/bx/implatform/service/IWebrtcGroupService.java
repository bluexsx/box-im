package com.bx.implatform.service;

import com.bx.implatform.dto.*;

public interface IWebrtcGroupService {

    /**
     * 发起通话
     * @param dto
     */
    void setup(WebrtcGroupSetupDTO dto);

    /**
     * 接受通话
     * @groupId 群id
     */
    void accept(Long groupId);

    /**
     * 拒绝通话
     * @groupId 群id
     */
    void reject(Long groupId);

    /**
     * 通话失败,如设备不支持、用户忙等(此接口为系统自动调用,无需用户操作，所以不抛异常)
     * @dto dto
     */
    void failed(WebrtcGroupFailedDTO dto);

    /**
     * 主动加入通话
     * @groupId 群id
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
     * @dto dto
     */
    void offer(WebrtcGroupOfferDTO dto);

    /**
     * 推送answer信息给对方
     * @dto dto
     */
    void answer(WebrtcGroupAnswerDTO dto);

    /**
     * 推送candidate信息给对方
     * @dto dto
     */
    void candidate(WebrtcGroupCandidateDTO dto);

    /**
     * 用户进行了设备操作，如果关闭摄像头
     * @dto dto
     */
    void device(WebrtcGroupDeviceDTO dto);




}
