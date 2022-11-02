package com.lx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lx.common.contant.RedisKey;
import com.lx.implatform.entity.GroupMember;
import com.lx.implatform.mapper.GroupMemberMapper;
import com.lx.implatform.service.IGroupMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP_MEMBER)
@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements IGroupMemberService {


    /**
     * 添加群聊成员
     *
     * @param member 成员
     * @return
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
     * @return
     */
    @CacheEvict(key="#groupId")
    @Override
    public boolean saveBatch(long groupId,List<GroupMember> members) {
        return super.saveBatch(members);
    }

    /**
     * 根据群聊id和用户id查询群聊成员
     *
     * @param groupId 群聊id
     * @param userId 用户id
     * @return
     */
    @Override
    public GroupMember findByGroupAndUserId(long groupId, long userId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId,groupId)
                .eq(GroupMember::getUserId,userId);
        return this.getOne(wrapper);
    }

    /**
     * 根据用户id查询群聊成员
     *
     * @param userId 用户id
     * @return
     */
    @Override
    public List<GroupMember> findByUserId(long userId) {
        QueryWrapper<GroupMember> memberWrapper = new QueryWrapper();
        memberWrapper.lambda().eq(GroupMember::getUserId, userId);
        return this.list(memberWrapper);
    }

    /**
     * 根据群聊id查询群聊成员
     *
     * @param groupId 群聊id
     * @return
     */
    @Override
    public List<GroupMember> findByGroupId(long groupId) {
        QueryWrapper<GroupMember> memberWrapper = new QueryWrapper();
        memberWrapper.lambda().eq(GroupMember::getGroupId, groupId);
        return this.list(memberWrapper);
    }

    /**
     *根据群聊id删除成员信息
     *
     * @param groupId  群聊id
     * @return
     */
    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupId(long groupId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper();
        wrapper.lambda().eq(GroupMember::getGroupId,groupId);
        this.remove(wrapper);
    }

    /**
     *根据群聊id和用户id删除成员信息
     *
     * @param groupId  群聊id
     * @param userId  用户id
     * @return
     */
    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupAndUserId(long groupId, long userId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId,groupId)
                .eq(GroupMember::getUserId,userId);
        this.remove(wrapper);
    }
}
