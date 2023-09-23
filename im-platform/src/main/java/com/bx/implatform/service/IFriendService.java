package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.vo.FriendVO;

import java.util.List;


public interface IFriendService extends IService<Friend> {

    Boolean isFriend(Long userId1, Long userId2);

    List<Friend> findFriendByUserId(Long userId);

    void addFriend(Long friendId);

    void delFriend(Long friendId);

    void update(FriendVO vo);

    FriendVO findFriend(Long friendId);
}
