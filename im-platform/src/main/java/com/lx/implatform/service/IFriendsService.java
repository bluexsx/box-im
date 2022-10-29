package com.lx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.implatform.entity.Friends;
import com.lx.implatform.vo.FriendsVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author blue
 * @since 2022-10-22
 */
public interface IFriendsService extends IService<Friends> {

    Boolean isFriends(Long userId1,long userId2);

    List<Friends> findFriendsByUserId(long UserId);

    void addFriends(long friendId);

    void delFriends(long friendId);

    void update(FriendsVO vo);

}
