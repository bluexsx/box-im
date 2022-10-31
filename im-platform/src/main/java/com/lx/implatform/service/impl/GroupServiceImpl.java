package com.lx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lx.common.enums.ResultCode;
import com.lx.common.util.BeanUtils;
import com.lx.implatform.entity.Friends;
import com.lx.implatform.entity.Group;
import com.lx.implatform.entity.GroupMember;
import com.lx.implatform.entity.User;
import com.lx.implatform.exception.GlobalException;
import com.lx.implatform.mapper.GroupMapper;
import com.lx.implatform.service.IFriendsService;
import com.lx.implatform.service.IGroupMemberService;
import com.lx.implatform.service.IGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lx.implatform.service.IUserService;
import com.lx.implatform.session.SessionContext;
import com.lx.implatform.session.UserSession;
import com.lx.implatform.vo.GroupInviteVO;
import com.lx.implatform.vo.GroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {

    @Autowired
    private IUserService userService;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Autowired
    private IFriendsService friendsService;

    /**
     * 创建新群聊
     *
     * @return GroupVO
     * @Param groupName 群聊名称
     **/
    @Transactional
    @Override
    public GroupVO createGroup(String groupName) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getId());
        // 保存群组数据
        Group group = new Group();
        group.setName(groupName);
        group.setOwnerId(user.getId());
        group.setHeadImage(user.getHeadImage());
        group.setHeadImageThumb(user.getHeadImageThumb());
        this.save(group);
        // 把群主加入群
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(group.getId());
        groupMember.setUserId(user.getId());
        groupMember.setAliasName(user.getNickName());
        groupMember.setHeadImage(user.getHeadImageThumb());
        groupMemberService.save(groupMember);
        GroupVO vo = BeanUtils.copyProperties(group, GroupVO.class);
        return vo;
    }

    /**
     * 修改群聊信息
     * 
     * @Param  GroupVO  群聊信息
     * @return GroupVO
     **/
    @Override
    public GroupVO modifyGroup(GroupVO vo) {
        UserSession session = SessionContext.getSession();
        // 校验是不是群主，只有群主能改信息
        Group group = this.getById(vo.getId());
        if(group.getOwnerId() != session.getId()){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"您不是群主,不可修改群信息");
        }
        // 保存群信息
        group = BeanUtils.copyProperties(vo,Group.class);
        this.save(group);
        return vo;
    }

    /**
     * 删除群聊
     * 
     * @Param groupId 群聊id
     * @return void
     **/
    @Transactional
    @Override
    public void deleteGroup(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        if(group == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"群组不存在");
        }
        if(group.getOwnerId() != session.getId()){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"只有群主才有权限解除群聊");
        }
        // 删除群数据
        this.removeById(groupId);
        // 删除成员数据
        QueryWrapper<GroupMember> wrapper = new QueryWrapper();
        wrapper.lambda().eq(GroupMember::getGroupId,groupId);
        groupMemberService.remove(wrapper);
    }

    /**
     * 查询当前用户的所有群聊
     *
     * @return List<GroupVO>
     **/
    @Override
    public List<GroupVO> findGroups() {
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群id列表
        QueryWrapper<GroupMember> memberWrapper = new QueryWrapper();
        memberWrapper.lambda().eq(GroupMember::getUserId, session.getId());
        List<GroupMember> groupMembers = groupMemberService.list(memberWrapper);
        // 拉取群信息
        List<Long> ids = groupMembers.stream().map((gm -> gm.getGroupId())).collect(Collectors.toList());
        QueryWrapper<Group> groupWrapper = new QueryWrapper();
        groupWrapper.lambda().in(Group::getId, ids);
        List<Group> groups = this.list(groupWrapper);
        // 转vo
        List<GroupVO> vos = groups.stream().map(g -> {
            GroupVO vo = BeanUtils.copyProperties(g, GroupVO.class);
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }

    /**
     * 邀请好友进群
     *
     * @return
     * @Param GroupInviteVO  群id、好友id列表
     **/
    @Override
    public void invite(GroupInviteVO vo) {
        UserSession session = SessionContext.getSession();
        // 已经在群里面用户，不可重复加入
        QueryWrapper<GroupMember> wrapper = new QueryWrapper();
        wrapper.lambda().eq(GroupMember::getId, vo.getGroupId())
                .in(GroupMember::getUserId, vo.getFriendIds());
        if(groupMemberService.count(wrapper)>0){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "部分用户已经在群中，邀请失败");
        }
        // 找出好友信息
        List<Friends> friends = friendsService.findFriendsByUserId(session.getId());
        List<Friends> friendsList = vo.getFriendIds().stream().map(id ->
                friends.stream().filter(f -> f.getFriendId().equals(id)).findFirst().get()).collect(Collectors.toList());
        if (friendsList.size() != vo.getFriendIds().size()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "部分用户不是您的好友，邀请失败");
        }
        // 批量保存成员数据
        List<GroupMember> groupMembers = friendsList.stream()
                .map(f -> {
                    GroupMember groupMember = new GroupMember();
                    groupMember.setGroupId(vo.getGroupId());
                    groupMember.setUserId(f.getFriendId());
                    groupMember.setAliasName(f.getFriendNickName());
                    groupMember.setHeadImage(f.getFriendHeadImage());
                    return groupMember;
                }).collect(Collectors.toList());
        if(!groupMembers.isEmpty()) {
            groupMemberService.saveBatch(groupMembers);
        }
    }
}
