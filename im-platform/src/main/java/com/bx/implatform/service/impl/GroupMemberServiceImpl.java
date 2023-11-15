package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.mapper.GroupMemberMapper;
import com.bx.implatform.service.IGroupMemberService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP_MEMBER_ID)
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements IGroupMemberService {


    /**
     * 添加群聊成员
     *
     * @param member 成员
     * @return  成功或失败
     */
    @CacheEvict(key="#member.getGroupId()")
    @Override
    public boolean save(GroupMember member) {
        return super.save(member);
    }


    /**
     * 批量添加成员
     *
     * @param groupId 群聊id
     * @param members 成员列表
     * @return 成功或失败
     */
    @CacheEvict(key="#groupId")
    @Override
    public boolean saveOrUpdateBatch(Long groupId,List<GroupMember> members) {
        return super.saveOrUpdateBatch(members);
    }

    /**
     * 根据群聊id和用户id查询群聊成员
     *
     * @param groupId 群聊id
     * @param userId 用户id
     * @return 群聊成员信息
     */
    @Override
    public GroupMember findByGroupAndUserId(Long groupId, Long userId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId,groupId)
                .eq(GroupMember::getUserId,userId);
        return this.getOne(wrapper);
    }


    /**
     * 根据用户id查询群聊成员
     *
     * @param userId 用户id
     * @return 成员列表
     */
    @Override
    public List<GroupMember> findByUserId(Long userId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getQuit,false);
        return this.list(memberWrapper);
    }


    /**
     * 根据群聊id查询群聊成员（包括已退出）
     *
     * @param groupId 群聊id
     * @return 群聊成员列表
     */
    @Override
    public List<GroupMember> findByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId);
        return this.list(memberWrapper);
    }


    /**
     * 根据群聊id查询没有退出的群聊成员id
     *
     * @param groupId 群聊id
     * @return 群聊成员id列表
     */
    @Cacheable(key="#groupId")
    @Override
    public List<Long> findUserIdsByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getQuit,false);
        List<GroupMember> members = this.list(memberWrapper);
        return members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
    }


    /**
     * 根据群聊id删除移除成员
     *
     * @param groupId  群聊id
     */
    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupId(Long groupId) {
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId,groupId)
                .set(GroupMember::getQuit,true);
        this.update(wrapper);
    }

    /**
     *根据群聊id和用户id移除成员
     *
     * @param groupId  群聊id
     * @param userId  用户id
     */
    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupAndUserId(Long groupId, Long userId) {
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId,groupId)
                .eq(GroupMember::getUserId,userId)
                .set(GroupMember::getQuit,true);
        this.update(wrapper);
    }
}
