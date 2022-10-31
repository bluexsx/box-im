package com.lx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lx.common.enums.ResultCode;
import com.lx.implatform.entity.Friend;
import com.lx.implatform.entity.User;
import com.lx.implatform.exception.GlobalException;
import com.lx.implatform.mapper.FriendMapper;
import com.lx.implatform.service.IFriendService;
import com.lx.implatform.service.IUserService;
import com.lx.implatform.session.SessionContext;
import com.lx.implatform.vo.FriendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  好友服务实现类
 * </p>
 *
 * @author blue
 * @since 2022-10-22
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {

    @Autowired
    private IUserService userService;

    @Override
    public List<Friend> findFriendByUserId(long UserId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Friend::getUserId,UserId);
        List<Friend> friends = this.list(queryWrapper);
        return friends;
    }


    @Transactional
    @Override
    public void addFriend(long friendId) {
        long userId = SessionContext.getSession().getId();
        if(userId == friendId){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"不允许添加自己为好友");
        }
        // 互相绑定好友关系
        bindFriend(userId,friendId);
        bindFriend(friendId,userId);
    }


    @Transactional
    @Override
    public void delFriend(long friendId) {
        long userId = SessionContext.getSession().getId();
        // 互相解除好友关系
        unbindFriend(userId,friendId);
        unbindFriend(friendId,userId);
    }



    @Override
    public Boolean isFriend(Long userId1, long userId2) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId,userId1)
                .eq(Friend::getFriendId,userId2);
        return  this.count(queryWrapper) > 0;
    }


    @Override
    public void update(FriendVO vo) {
        long userId = SessionContext.getSession().getId();
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId,userId)
                .eq(Friend::getFriendId,vo.getFriendId());

        Friend f = this.getOne(queryWrapper);
        if(f == null){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"对方不是您的好友");
        }

        f.setFriendHeadImage(vo.getFriendHeadImage());
        f.setFriendNickName(vo.getFriendNickName());
        this.updateById(f);
    }

    private void bindFriend(long userId, long friendId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId,userId)
                .eq(Friend::getFriendId,friendId);
        if(this.count(queryWrapper)==0){
            Friend friend = new Friend();
            friend.setUserId(userId);
            friend.setFriendId(friendId);
            User friendInfo = userService.getById(friendId);
            friend.setFriendHeadImage(friendInfo.getHeadImage());
            friend.setFriendNickName(friendInfo.getNickName());
            this.save(friend);
        }
    }


    private void unbindFriend(long userId, long friendId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId,userId)
                .eq(Friend::getFriendId,friendId);
        List<Friend> friends = this.list(queryWrapper);
        friends.stream().forEach(friend -> {
            this.removeById(friend.getId());
        });
    }


}
