package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.User;
import com.bx.implatform.vo.RegisterVO;
import com.bx.implatform.vo.UserVO;

import java.util.List;


public interface IUserService extends IService<User> {

    void register(RegisterVO registerDTO);

    User findUserByName(String username);

    void update(UserVO vo);

    List<UserVO> findUserByNickName(String nickname);

    List<Long> checkOnline(String userIds);

}
