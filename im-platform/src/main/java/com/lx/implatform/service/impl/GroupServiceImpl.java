package com.lx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lx.common.contant.Constant;
import com.lx.common.enums.ResultCode;
import com.lx.common.util.BeanUtils;
import com.lx.implatform.entity.Friend;
import com.lx.implatform.entity.Group;
import com.lx.implatform.entity.GroupMember;
import com.lx.implatform.entity.User;
import com.lx.implatform.exception.GlobalException;
import com.lx.implatform.mapper.GroupMapper;
import com.lx.implatform.service.IFriendService;
import com.lx.implatform.service.IGroupMemberService;
import com.lx.implatform.service.IGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lx.implatform.service.IUserService;
import com.lx.implatform.session.SessionContext;
import com.lx.implatform.session.UserSession;
import com.lx.implatform.vo.GroupInviteVO;
import com.lx.implatform.vo.GroupMemberVO;
import com.lx.implatform.vo.GroupVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {

    @Autowired
    private IUserService userService;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Autowired
    private IFriendService friendsService;

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
     * @Param  GroupVO 群聊信息
     * @return GroupVO
     **/
    @Transactional
    @Override
    public GroupVO modifyGroup(GroupVO vo) {
        UserSession session = SessionContext.getSession();
        // 校验是不是群主，只有群主能改信息
        Group group = this.getById(vo.getId());
        // 群主有权修改群基本信息
        if(group.getOwnerId() == session.getId()){
            group = BeanUtils.copyProperties(vo,Group.class);
            this.save(group);
        }
        // 更新成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(vo.getId(),session.getId());
        if(member == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您不是群聊的成员");
        }
        member.setAliasName(StringUtils.isEmpty(vo.getAliasName())?session.getNickName():vo.getAliasName());
        member.setRemark(StringUtils.isEmpty(vo.getRemark())?group.getName():vo.getRemark());
        groupMemberService.updateById(member);
        return vo;
    }

    /**
     * 删除群聊
     * 
     * @Param groupId 群聊id
     * @return
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
        groupMemberService.removeByGroupId(groupId);
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
        List<GroupMember> groupMembers = groupMemberService.findByUserId(session.getId());
        if(groupMembers.isEmpty()){
            return Collections.EMPTY_LIST;
        }
        // 拉取群信息
        List<Long> ids = groupMembers.stream().map((gm -> gm.getGroupId())).collect(Collectors.toList());
        QueryWrapper<Group> groupWrapper = new QueryWrapper();
        groupWrapper.lambda().in(Group::getId, ids);
        List<Group> groups = this.list(groupWrapper);
        // 转vo
        List<GroupVO> vos = groups.stream().map(g -> {
            GroupVO vo = BeanUtils.copyProperties(g, GroupVO.class);
            GroupMember member = groupMembers.stream().filter(m -> g.getId() == m.getGroupId()).findFirst().get();
            vo.setAliasName(member.getAliasName());
            vo.setRemark(member.getRemark());
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }

    /**
     * 邀请好友进群
     *
     * @Param GroupInviteVO  群id、好友id列表
     * @return
     **/
    @Override
    public void invite(GroupInviteVO vo) {
        UserSession session = SessionContext.getSession();
        // 群聊人数校验
        List<GroupMember> members = groupMemberService.findByGroupId(vo.getGroupId());
        if(vo.getFriendIds().size() + members.size() > Constant.MAX_GROUP_MEMBER){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊人数不能大于"+Constant.MAX_GROUP_MEMBER+"人");
        }
        // 已经在群里面用户，不可重复加入
        Boolean flag = vo.getFriendIds().stream().anyMatch(id->{
           return  members.stream().anyMatch(m->m.getUserId()==id);
        });
        if(flag){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "部分用户已经在群中，邀请失败");
        }
        // 找出好友信息
        List<Friend> friends = friendsService.findFriendByUserId(session.getId());
        List<Friend> friendsList = vo.getFriendIds().stream().map(id ->
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

    /**
     * 查询群成员
     *
     * @Param groupId 群聊id
     * @return List<GroupMemberVO>
     **/
    @Override
    public List<GroupMemberVO> findGroupMembers(Long groupId) {
        List<GroupMember> members = groupMemberService.findByGroupId(groupId);
        List<GroupMemberVO> vos = members.stream().map(m->{
            GroupMemberVO vo = BeanUtils.copyProperties(m,GroupMemberVO.class);
            return  vo;
        }).collect(Collectors.toList());
        return vos;
    }
}
