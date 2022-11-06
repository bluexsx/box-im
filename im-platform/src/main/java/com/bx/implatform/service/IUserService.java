package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.vo.RegisterVO;
import com.bx.implatform.vo.UserVO;
import com.bx.implatform.entity.User;

import java.util.List;

/**
 * <p>
 *  用户服务类
 * </p>
 *
 * @author blue
 * @since 2022-10-01
 */
public interface IUserService extends IService<User> {

    void register(RegisterVO registerDTO);

    User findUserByName(String username);

    void update(UserVO vo);

    List<UserVO> findUserByNickName(String nickname);

    List<Long> checkOnline(String userIds);

}
