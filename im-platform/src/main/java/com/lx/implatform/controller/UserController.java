package com.lx.implatform.controller;


import com.lx.common.result.Result;
import com.lx.common.result.ResultUtils;
import com.lx.common.util.BeanUtils;
import com.lx.implatform.entity.User;
import com.lx.implatform.service.IUserService;
import com.lx.implatform.session.SessionContext;
import com.lx.implatform.session.UserSession;
import com.lx.implatform.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;


@Api(tags = "用户相关API")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;


    @GetMapping("/online")
    public Result checkOnline(@NotEmpty @RequestParam("userIds") String userIds){
        List<Long> onlineIds = userService.checkOnline(userIds);
        return ResultUtils.success(onlineIds);
    }

    @GetMapping("/self")
    public Result findSelfInfo(){
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getId());
        UserVO userVO = BeanUtils.copyProperties(user,UserVO.class);
        return ResultUtils.success(userVO);
    }


    @GetMapping("/find/{id}")
    public Result findByIde(@NotEmpty @PathVariable("id") long id){
        User user = userService.getById(id);
        UserVO userVO = BeanUtils.copyProperties(user,UserVO.class);
        return ResultUtils.success(userVO);
    }


    @GetMapping("/findByNickName")
    @ApiOperation(value = "查找非好友用户",notes="查找非好友用户")
    public Result findUnfriendsUser(@NotEmpty(message = "用户昵称不可为空") @RequestParam("nickName") String nickName){
           return ResultUtils.success( userService.findUserByNickName(nickName));
    }
}

