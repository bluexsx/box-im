package com.lx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lx.common.contant.RedisKey;
import com.lx.common.enums.ResultCode;
import com.lx.common.util.BeanUtils;
import com.lx.implatform.entity.User;
import com.lx.implatform.exception.GlobalException;
import com.lx.implatform.mapper.UserMapper;
import com.lx.implatform.service.IUserService;
import com.lx.implatform.session.SessionContext;
import com.lx.implatform.session.UserSession;
import com.lx.implatform.vo.RegisterVO;
import com.lx.implatform.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  用户服务实现类
 * </p>
 *
 * @author blue
 * @since 2022-10-01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Override
    public User findUserByName(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getUserName,username);
        return this.getOne(queryWrapper);
    }


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

        user.setNickName(vo.getNickName());
        user.setSex(vo.getSex());
        user.setSignature(vo.getSignature());
        user.setHeadImage(vo.getHeadImage());
        user.setHeadImageThumb(vo.getHeadImageThumb());
        this.updateById(user);
    }

    @Override
    public List<UserVO> findUserByNickName(String nickname) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(User::getNickName,nickname)
                .last("limit 10");
        List<User> users = this.list(queryWrapper);
        List<UserVO> vos = users.stream().map(u-> {
            UserVO vo = BeanUtils.copyProperties(u,UserVO.class);
            vo.setOnline(isOnline(u.getId()));
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }


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
