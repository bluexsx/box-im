package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.GroupMember;

import java.util.Date;
import java.util.List;

public interface GroupMemberService extends IService<GroupMember> {

    /**
     * 根据群聊id和用户id查询群聊成员
     *
     * @param groupId 群聊id
     * @param userId  用户id
     * @return 群聊成员信息
     */
    GroupMember findByGroupAndUserId(Long groupId, Long userId);


    /**
     * 根据用户id查询群聊成员
     *
     * @param userId 用户id
     * @return 成员列表
     */
    List<GroupMember> findByUserId(Long userId);


    /**
     * 根据群聊id和用户id查询群聊成员
     *
     * @param groupId 群聊id
     * @param userIds 用户id
     * @return 群聊成员信息
     */
    List<GroupMember> findByGroupAndUserIds(Long groupId, List<Long> userIds);

    /**
     * 根据用户id查询某段时间内退的群
     *
     * @param userId      用户id
     * @param minQuitTime 退群时间
     * @return 成员列表
     */
    public List<GroupMember> findQuitMembers(Long userId, Date minQuitTime);

    /**
     * 根据群聊id查询群聊成员（包括已退出）
     *
     * @param groupId 群聊id
     * @param version 版本号
     * @return 群聊成员列表
     */
    List<GroupMember> findByGroupId(Long groupId, Long version);



    /**
     * 根据群聊id查询没有退出的群聊成员id
     *
     * @param groupId 群聊id
     * @return 群聊成员id列表
     */
    List<Long> findUserIdsByGroupId(Long groupId);

    /**
     * 批量添加成员
     *
     * @param groupId 群聊id
     * @param members 成员列表
     * @return 成功或失败
     */
    boolean saveOrUpdateBatch(Long groupId, List<GroupMember> members);

    /**
     * 根据群聊id删除移除成员
     *
     * @param groupId 群聊id
     */
    void removeByGroupId(Long groupId);

    /**
     * 根据群聊id和用户id移除成员
     *
     * @param groupId 群聊id
     * @param userId  用户id
     */
    void removeByGroupAndUserId(Long groupId, Long userId);

    /**
     * 根据群聊id和用户id移除成员
     *
     * @param groupId 群聊id
     * @param userIds  用户id
     */
    void removeByGroupAndUserIds(Long groupId, List<Long> userIds);

    /**
     * 用户用户是否在群中
     *
     * @param groupId 群聊id
     * @param userIds  用户id
     */
    Boolean isInGroup(Long groupId,List<Long> userIds);

    /**
     * 设置免打扰状态
     * @param groupId 群id
     * @param userId 用户id
     * @param isDnd 是否开启免打扰
     */
    void setDnd(Long groupId, Long userId, Boolean isDnd);

    /**
     * 设置会话置顶状态
     * @param groupId 群id
     * @param userId 用户id
     * @param isTop 是否置顶
     */
    void setTop(Long groupId, Long userId, Boolean isTop);

    /**
     * 获取群内成员最大版本号
     *
     * @return
     */
    Long getNextVersion();


    @Override
    boolean saveOrUpdate(GroupMember member);
}
