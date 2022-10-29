package com.lx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lx.common.enums.ResultCode;
import com.lx.implatform.entity.Friends;
import com.lx.implatform.entity.User;
import com.lx.implatform.exception.GlobalException;
import com.lx.implatform.mapper.FriendsMapper;
import com.lx.implatform.service.IFriendsService;
import com.lx.implatform.service.IUserService;
import com.lx.implatform.session.SessionContext;
import com.lx.implatform.vo.FriendsVO;
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
public class FriendsServiceImpl extends ServiceImpl<FriendsMapper, Friends> implements IFriendsService {

    @Autowired
    private IUserService userService;

    @Override
    public List<Friends> findFriendsByUserId(long UserId) {
        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Friends::getUserId,UserId);
        List<Friends> friendsList = this.list(queryWrapper);
        return friendsList;
    }


    @Transactional
    @Override
    public void addFriends(long friendId) {
        long userId = SessionContext.getSession().getId();
        if(userId == friendId){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"不允许添加自己为好友");
        }
        // 互相绑定好友关系
        bindFriends(userId,friendId);
        bindFriends(friendId,userId);
    }


    @Transactional
    @Override
    public void delFriends(long friendId) {
        long userId = SessionContext.getSession().getId();
        // 互相解除好友关系
        unbindFriends(userId,friendId);
        unbindFriends(friendId,userId);
    }



    @Override
    public Boolean isFriends(Long userId1, long userId2) {
        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friends::getUserId,userId1)
                .eq(Friends::getFriendId,userId2);
        return  this.count(queryWrapper) > 0;
    }


    @Override
    public void update(FriendsVO vo) {
        long userId = SessionContext.getSession().getId();
        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friends::getUserId,userId)
                .eq(Friends::getFriendId,vo.getFriendId());

        Friends f = this.getOne(queryWrapper);
        if(f == null){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"对方不是您的好友");
        }

        f.setFriendHeadImage(vo.getFriendHeadImage());
        f.setFriendNickName(vo.getFriendNickName());
        this.updateById(f);
    }

    private void bindFriends(long userId, long friendsId) {
        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friends::getUserId,userId)
                .eq(Friends::getFriendId,friendsId);
        if(this.count(queryWrapper)==0){
            Friends friends = new Friends();
            friends.setUserId(userId);
            friends.setFriendId(friendsId);
            User friendsInfo = userService.getById(friendsId);
            friends.setFriendHeadImage(friendsInfo.getHeadImage());
            friends.setFriendNickName(friendsInfo.getNickName());
            this.save(friends);
        }
    }


    private void unbindFriends(long userId, long friendsId) {
        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friends::getUserId,userId)
                .eq(Friends::getFriendId,friendsId);
        List<Friends> friendsList = this.list(queryWrapper);
        friendsList.stream().forEach(friends -> {
            this.removeById(friends.getId());
        });
    }


}
