package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.common.contant.RedisKey;
import com.bx.common.enums.ResultCode;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.User;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.FriendMapper;
import com.bx.implatform.service.IFriendService;
import com.bx.implatform.service.IUserService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.vo.FriendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@CacheConfig(cacheNames= RedisKey.IM_CACHE_FRIEND)
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {

    @Autowired
    private IUserService userService;

    /**
     * 查询用户的所有好友
     *
     * @param UserId   用户id
     * @return
     */
    @Override
    public List<Friend> findFriendByUserId(Long UserId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(Friend::getUserId,UserId);
        List<Friend> friends = this.list(queryWrapper);
        return friends;
    }


    /**
     * 添加好友，互相建立好友关系
     *
     * @param friendId 好友的用户id
     * @return
     */
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
        log.info("添加好友，用户id:{},好友id:{}",userId,friendId);
    }


    /**
     * 删除好友，双方都会解除好友关系
     *
     * @param friendId 好友的用户id
     * @return
     */
    @Transactional
    @Override
    public void delFriend(Long friendId) {
        long userId = SessionContext.getSession().getId();
        // 互相解除好友关系
        FriendServiceImpl proxy = (FriendServiceImpl)AopContext.currentProxy();
        proxy.unbindFriend(userId,friendId);
        proxy.unbindFriend(friendId,userId);
        log.info("删除好友，用户id:{},好友id:{}",userId,friendId);
    }


    /**
     * 判断用户2是否用户1的好友
     *
     * @param userId1 用户1的id
     * @param userId2 用户2的id
     * @return
     */
    @Cacheable(key="#userId1+':'+#userId2")
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId,userId1)
                .eq(Friend::getFriendId,userId2);
        return  this.count(queryWrapper) > 0;
    }


    /**
     * 更新好友信息，主要是头像和昵称
     *
     * @param vo  好友vo
     * @return
     */
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


    /**
     * 单向绑定好友关系
     *
     * @param userId  用户id
     * @param friendId  好友的用户id
     * @return
     */
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


    /**
     * 单向解除好友关系
     *
     * @param userId  用户id
     * @param friendId  好友的用户id
     * @return
     */
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


    /**
     * 查询指定的某个好友信息
     *
     * @param friendId 好友的用户id
     * @return
     */
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
