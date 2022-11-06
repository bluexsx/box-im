package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.common.contant.RedisKey;
import com.bx.common.enums.ResultCode;
import com.bx.common.util.BeanUtils;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.entity.User;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.UserMapper;
import com.bx.implatform.service.IFriendService;
import com.bx.implatform.service.IGroupMemberService;
import com.bx.implatform.service.IUserService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.vo.RegisterVO;
import com.bx.implatform.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Autowired
    private IFriendService friendService;

    /**
     * 用户注册
     *
     * @param vo 注册vo
     * @return
     */
    @Override
    public void register(RegisterVO vo) {
        User user = findUserByName(vo.getUserName());
        if(null != user){
            throw  new GlobalException(ResultCode.USERNAME_ALREADY_REGISTER);
        }
        user = BeanUtils.copyProperties(vo,User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        this.save(user);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return
     */
    @Override
    public User findUserByName(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getUserName,username);
        return this.getOne(queryWrapper);
    }

    /**
     * 更新用户信息，好友昵称和群聊昵称等冗余信息也会更新
     *
     * @param vo 用户信息vo
     * @return
     */
    @Transactional
    @Override
    public void update(UserVO vo) {
        UserSession session = SessionContext.getSession();
        if(session.getId() != vo.getId()){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"不允许修改其他用户的信息!");
        }
        User user = this.getById(vo.getId());
        if(null == user){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"用户不存在");
        }
        // 更新好友昵称和头像
        if(!user.getNickName().equals(vo.getNickName()) || !user.getHeadImageThumb().equals(vo.getHeadImageThumb())){
            QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Friend::getFriendId,session.getId());
            List<Friend> friends = friendService.list(queryWrapper);
            for(Friend friend: friends){
                friend.setFriendNickName(vo.getNickName());
                friend.setFriendHeadImage(vo.getHeadImageThumb());
            }
            friendService.updateBatchById(friends);
        }
        // 更新群聊中的头像
        if(!user.getHeadImageThumb().equals(vo.getHeadImageThumb())){
            List<GroupMember> members = groupMemberService.findByUserId(session.getId());
            for(GroupMember member:members){
                member.setHeadImage(vo.getHeadImageThumb());
            }
            groupMemberService.updateBatchById(members);
        }
        // 更新用户信息
        user.setNickName(vo.getNickName());
        user.setSex(vo.getSex());
        user.setSignature(vo.getSignature());
        user.setHeadImage(vo.getHeadImage());
        user.setHeadImageThumb(vo.getHeadImageThumb());
        this.updateById(user);
    }


    /**
     * 根据用户昵称查询用户，最多返回20条数据
     *
     * @param nickname 用户昵称
     * @return
     */
    @Override
    public List<UserVO> findUserByNickName(String nickname) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(User::getNickName,nickname)
                .last("limit 20");
        List<User> users = this.list(queryWrapper);
        List<UserVO> vos = users.stream().map(u-> {
            UserVO vo = BeanUtils.copyProperties(u,UserVO.class);
            vo.setOnline(isOnline(u.getId()));
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }


    /**
     * 判断用户是否在线，返回在线的用户id列表
     *
     * @param userIds 用户id，多个用‘,’分割
     * @return
     */
    @Override
    public List<Long> checkOnline(String userIds) {
        String[] idArr = userIds.split(",");
        List<Long> onlineIds = new LinkedList<>();
        for(String userId:idArr){
           if(isOnline(Long.parseLong(userId))){
                onlineIds.add(Long.parseLong(userId));
            }
        }
        return onlineIds;
    }


    private boolean isOnline(Long userId){
        String key = RedisKey.IM_USER_SERVER_ID + userId;
        Integer serverId = (Integer) redisTemplate.opsForValue().get(key);
        return serverId!=null && serverId>=0;
    }
}
