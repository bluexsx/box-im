package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.common.contant.RedisKey;
import com.bx.common.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.vo.FriendVO;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.User;
import com.bx.implatform.mapper.FriendMapper;
import com.bx.implatform.service.IFriendService;
import com.bx.implatform.service.IUserService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
@CacheConfig(cacheNames= RedisKey.IM_CACHE_FRIEND)
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {

    @Autowired
    private IUserService userService;

    @Override
    public List<Friend> findFriendByUserId(Long UserId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(Friend::getUserId,UserId);
        List<Friend> friends = this.list(queryWrapper);
        return friends;
    }


    @Transactional
    @Override
    public void addFriend(Long friendId) {
        long userId = SessionContext.getSession().getId();
        if(userId == friendId){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"不允许添加自己为好友");
        }
        // 互相绑定好友关系
        FriendServiceImpl proxy = (FriendServiceImpl)AopContext.currentProxy();
        proxy.bindFriend(userId,friendId);
        proxy.bindFriend(friendId,userId);
    }


    @Transactional
    @Override
    public void delFriend(Long friendId) {
        long userId = SessionContext.getSession().getId();
        // 互相解除好友关系
        FriendServiceImpl proxy = (FriendServiceImpl)AopContext.currentProxy();
        proxy.unbindFriend(userId,friendId);
        proxy.unbindFriend(friendId,userId);
    }


    @Cacheable(key="#userId1+':'+#userId2")
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
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
                .eq(Friend::getFriendId,vo.getId());

        Friend f = this.getOne(queryWrapper);
        if(f == null){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"对方不是您的好友");
        }

        f.setFriendHeadImage(vo.getHeadImage());
        f.setFriendNickName(vo.getNickName());
        this.updateById(f);
    }

    @CacheEvict(key="#userId+':'+#friendId")
    public void bindFriend(Long userId, Long friendId) {
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

    @CacheEvict(key="#userId+':'+#friendId")
    public void unbindFriend(Long userId, Long friendId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId,userId)
                .eq(Friend::getFriendId,friendId);
        List<Friend> friends = this.list(queryWrapper);
        friends.stream().forEach(friend -> {
            this.removeById(friend.getId());
        });
    }


    @Override
    public FriendVO findFriend(Long friendId) {
        UserSession session = SessionContext.getSession();
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Friend::getUserId,session.getId())
                .eq(Friend::getFriendId,friendId);
        Friend friend = this.getOne(wrapper);
        if(friend == null){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"对方不是您的好友");
        }
        FriendVO vo = new FriendVO();
        vo.setId(friend.getFriendId());
        vo.setHeadImage(friend.getFriendHeadImage());
        vo.setNickName(friend.getFriendNickName());
        return  vo;
    }
}
