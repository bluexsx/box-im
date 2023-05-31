package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.User;
import com.bx.implatform.vo.LoginVO;
import com.bx.implatform.vo.RegisterVO;
import com.bx.implatform.vo.UserVO;

import java.util.List;


public interface IUserService extends IService<User> {

    String login(LoginVO vo);

    void register(RegisterVO vo);

    User findUserByName(String username);

    void update(UserVO vo);

    List<UserVO> findUserByNickName(String nickname);

    List<Long> checkOnline(String userIds);

}
