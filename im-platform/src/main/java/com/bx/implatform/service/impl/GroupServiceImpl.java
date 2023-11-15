package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.Group;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.GroupMapper;
import com.bx.implatform.service.IFriendService;
import com.bx.implatform.service.IGroupMemberService;
import com.bx.implatform.service.IGroupService;
import com.bx.implatform.service.IUserService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.GroupInviteVO;
import com.bx.implatform.vo.GroupMemberVO;
import com.bx.implatform.vo.GroupVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP)
@Service
@AllArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {
    private IUserService userService;
    private IGroupMemberService groupMemberService;
    private IFriendService friendsService;
    private IMClient imClient;

    /**
     * 创建新群聊
     *
     * @param  vo 群聊信息
     * @return 群聊信息
     **/
    @Override
    public GroupVO createGroup(GroupVO vo) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        // 保存群组数据
        Group group = BeanUtils.copyProperties(vo,Group.class);
        group.setOwnerId(user.getId());
        this.save(group);
        // 把群主加入群
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(group.getId());
        groupMember.setUserId(user.getId());
        groupMember.setAliasName(StringUtils.isEmpty(vo.getAliasName())?session.getNickName():vo.getAliasName());
        groupMember.setRemark(StringUtils.isEmpty(vo.getRemark())?group.getName():vo.getRemark());
        groupMemberService.save(groupMember);

        vo.setId(group.getId());
        vo.setAliasName(groupMember.getAliasName());
        vo.setRemark(groupMember.getRemark());
        log.info("创建群聊，群聊id:{},群聊名称:{}",group.getId(),group.getName());
        return vo;
    }

    /**
     * 修改群聊信息
     * 
     * @param  vo 群聊信息
     * @return 群聊信息
     **/
    @CacheEvict(value = "#vo.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO modifyGroup(GroupVO vo) {
        UserSession session = SessionContext.getSession();
        // 校验是不是群主，只有群主能改信息
        Group group = this.getById(vo.getId());
        // 群主有权修改群基本信息
        if(group.getOwnerId().equals(session.getUserId()) ){
            group = BeanUtils.copyProperties(vo,Group.class);
            this.updateById(group);
        }
        // 更新成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(vo.getId(),session.getUserId());
        if(member == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您不是群聊的成员");
        }
        member.setAliasName(StringUtils.isEmpty(vo.getAliasName())?session.getNickName():vo.getAliasName());
        member.setRemark(StringUtils.isEmpty(vo.getRemark())?group.getName():vo.getRemark());
        groupMemberService.updateById(member);
        log.info("修改群聊，群聊id:{},群聊名称:{}",group.getId(),group.getName());
        return vo;
    }


    /**
     * 删除群聊
     * 
     * @param groupId 群聊id
     **/
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "#groupId")
    @Override
    public void deleteGroup(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        if(!group.getOwnerId().equals(session.getUserId())){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"只有群主才有权限解除群聊");
        }
        // 逻辑删除群数据
        group.setDeleted(true);
        this.updateById(group);
        // 删除成员数据
        groupMemberService.removeByGroupId(groupId);
        log.info("删除群聊，群聊id:{},群聊名称:{}",group.getId(),group.getName());
    }


    /**
     * 退出群聊
     *
     * @param groupId 群聊id
     */
    @Override
    public void quitGroup(Long groupId) {
        Long userId = SessionContext.getSession().getUserId();
        Group group = this.getById(groupId);
        if(group.getOwnerId().equals(userId)){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您是群主，不可退出群聊");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId,userId);
        log.info("退出群聊，群聊id:{},群聊名称:{},用户id:{}",group.getId(),group.getName(),userId);
    }


    /**
     * 将用户踢出群聊
     *
     * @param groupId 群聊id
     * @param userId 用户id
     */
    @Override
    public void kickGroup(Long groupId, Long userId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        if(!group.getOwnerId().equals(session.getUserId()) ){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您不是群主，没有权限踢人");
        }
        if(userId.equals(session.getUserId())){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"亲，不能自己踢自己哟");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId,userId);
        log.info("踢出群聊，群聊id:{},群聊名称:{},用户id:{}",group.getId(),group.getName(),userId);
    }

    @Override
    public GroupVO findById(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId,session.getUserId());
        if(member == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您未加入群聊");
        }
        GroupVO vo = BeanUtils.copyProperties(group,GroupVO.class);
        vo.setAliasName(member.getAliasName());
        vo.setRemark(member.getRemark());
        return  vo;
    }

    /**
     * 根据id查找群聊，并进行缓存
     *
     * @param groupId 群聊id
     * @return 群聊实体
     */
    @Cacheable(value = "#groupId")
    @Override
    public  Group getById(Long groupId){
        Group group = super.getById(groupId);
        if(group == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"群组不存在");
        }
        if(group.getDeleted()){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"群组'"+group.getName()+"'已解散");
        }
        return group;
    }



    /**
     * 查询当前用户的所有群聊
     *
     * @return 群聊信息列表
     **/
    @Override
    public List<GroupVO> findGroups() {
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群id列表
        List<GroupMember> groupMembers = groupMemberService.findByUserId(session.getUserId());
        if(groupMembers.isEmpty()){
            return new LinkedList<>();
        }
        // 拉取群列表
        List<Long> ids = groupMembers.stream().map((GroupMember::getGroupId)).collect(Collectors.toList());
        LambdaQueryWrapper<Group> groupWrapper = Wrappers.lambdaQuery();
        groupWrapper.in(Group::getId, ids);
        List<Group> groups = this.list(groupWrapper);
        // 转vo
        return groups.stream().map(g -> {
            GroupVO vo = BeanUtils.copyProperties(g, GroupVO.class);
            GroupMember member = groupMembers.stream().filter(m -> g.getId().equals(m.getGroupId())).findFirst().get();
            vo.setAliasName(member.getAliasName());
            vo.setRemark(member.getRemark());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 邀请好友进群
     *
     * @Param GroupInviteVO  群id、好友id列表
     **/
    @Override
    public void invite(GroupInviteVO vo) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(vo.getGroupId());
        if(group == null){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        // 群聊人数校验
        List<GroupMember> members = groupMemberService.findByGroupId(vo.getGroupId());
        long size = members.stream().filter(m->!m.getQuit()).count();
        if(vo.getFriendIds().size() + size > Constant.MAX_GROUP_MEMBER){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊人数不能大于"+Constant.MAX_GROUP_MEMBER+"人");
        }
        // 找出好友信息
        List<Friend> friends = friendsService.findFriendByUserId(session.getUserId());
        List<Friend> friendsList = vo.getFriendIds().stream().map(id ->
                friends.stream().filter(f -> f.getFriendId().equals(id)).findFirst().get()).collect(Collectors.toList());
        if (friendsList.size() != vo.getFriendIds().size()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "部分用户不是您的好友，邀请失败");
        }
        // 批量保存成员数据
        List<GroupMember> groupMembers = friendsList.stream()
            .map(f -> {
                Optional<GroupMember> optional =  members.stream().filter(m->m.getUserId().equals(f.getFriendId())).findFirst();
                GroupMember groupMember = optional.orElseGet(GroupMember::new);
                groupMember.setGroupId(vo.getGroupId());
                groupMember.setUserId(f.getFriendId());
                groupMember.setAliasName(f.getFriendNickName());
                groupMember.setRemark(group.getName());
                groupMember.setHeadImage(f.getFriendHeadImage());
                groupMember.setCreatedTime(new Date());
                groupMember.setQuit(false);
                return groupMember;
            }).collect(Collectors.toList());
        if(!groupMembers.isEmpty()) {
            groupMemberService.saveOrUpdateBatch(group.getId(),groupMembers);
        }
        log.info("邀请进入群聊，群聊id:{},群聊名称:{},被邀请用户id:{}",group.getId(),group.getName(),vo.getFriendIds());
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
        List<Long> userIds =  members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        return members.stream().map(m->{
            GroupMemberVO vo = BeanUtils.copyProperties(m,GroupMemberVO.class);
            vo.setOnline(onlineUserIds.contains(m.getUserId()));
            return vo;
        }).sorted((m1,m2)-> m2.getOnline().compareTo(m1.getOnline())).collect(Collectors.toList());
    }

}
