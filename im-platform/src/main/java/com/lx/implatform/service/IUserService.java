package com.lx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.implatform.entity.User;
import com.lx.implatform.vo.RegisterVO;
import com.lx.implatform.vo.UserVO;

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
