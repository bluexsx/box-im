package com.lx.implatform.controller;


import com.lx.common.result.Result;
import com.lx.common.result.ResultUtils;
import com.lx.common.util.BeanUtils;
import com.lx.implatform.entity.Friends;
import com.lx.implatform.service.IFriendsService;
import com.lx.implatform.session.SessionContext;
import com.lx.implatform.vo.FriendsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "好友")
@RestController
@RequestMapping("/friends")
public class FriendsController {

    @Autowired
    private IFriendsService friendsService;

    @GetMapping("/list")
    @ApiOperation(value = "好友列表",notes="获取好友列表")
    public Result findFriends(){
        List<Friends> friendsList = friendsService.findFriendsByUserId(SessionContext.getSession().getId());
        List<FriendsVO> vos = friendsList.stream().map(f->{
            FriendsVO vo = BeanUtils.copyProperties(f,FriendsVO.class);
            return vo;
        }).collect(Collectors.toList());
        return ResultUtils.success(vos);
    }



    @PostMapping("/add")
    @ApiOperation(value = "添加好友",notes="双方建立好友关系")
    public Result addFriends(@NotEmpty(message = "好友id不可为空") @RequestParam("friendId") Long friendId){
         friendsService.addFriends(friendId);
         return ResultUtils.success();
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "删除好友",notes="解除好友关系")
    public Result delFriends(@NotEmpty(message = "好友id不可为空") @RequestParam("friendId") Long friendId){
        friendsService.delFriends(friendId);
        return ResultUtils.success();
    }



}

