package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.dto.FriendDndDTO;
import com.bx.implatform.dto.FriendTopDTO;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.vo.FriendVO;
import com.bx.implatform.vo.UserOnlineVO;

import java.util.List;

public interface FriendService extends IService<Friend> {

    /**
     * 判断用户2是否用户1的好友
     *
     * @param userId1 用户1的id
     * @param userId2 用户2的id
     * @return true/false
     */
    Boolean isFriend(Long userId1, Long userId2);

    /**
     * 查询用户的所有好友,包括已删除的
     *
     * @return 好友列表
     */
    List<Friend> findAllFriends();

    /**
     * 查询好友id列表
     *
     * @return
     */
    List<Long> findFriendIds();

    /**
     * 查询用户的所有好友
     *
     * @param friendIds 好友id
     * @return 好友列表
     */
    List<Friend> findByFriendIds(List<Long> friendIds);

    /**
     * 查询当前用户的所有好友
     *
     * @param version 版本号
     * @return 好友列表
     */
    List<FriendVO> findFriends(Long version);
    /**
     * 添加好友，互相建立好友关系
     *
     * @param userId 用户id
     * @param friendId 好友的用户id
     */
    void addFriend(Long userId,Long friendId);

    /**
     * 删除好友，双方都会解除好友关系
     *
     * @param friendId 好友的用户id
     */
    void delFriend(Long friendId);

    /**
     * 推送在线状态给所有好友
     *
     * @param userId   用户id
     * @param terminal 终端类型
     */
    void sendOnlineStatus(Long userId, Integer terminal);

    /**
     * 推送在线状态给所有好友
     *
     * @param userId   用户id
     * @param friendId 好友id
     * @param terminal 终端类型
     */
    void sendOnlineStatus(Long userId, Long friendId, Integer terminal);

    /**
     * 查询指定的某个好友信息
     *
     * @param friendId 好友的用户id
     * @return 好友信息
     */
    FriendVO findFriend(Long friendId);

    /**
     * 绑定好友关系
     *
     * @param userId   好友的id
     * @param friendId 好友的用户id
     * @return 好友信息
     */
    void bindFriend(Long userId, Long friendId);

    /**
     * 设置好友免打扰状态
     * @param dto
     */
    void setDnd(FriendDndDTO dto);

    /**
     * 设置好友会话置顶状态
     * @param dto
     */
    void setTop(FriendTopDTO dto);

    /**
     * 查询好友在线的终端列表
     *
     * @return
     */
    List<UserOnlineVO> findOnlineTerminals();


    /**
     * 获取全局版本号
     *
     * @return
     */
    Long getNextVersion();

}