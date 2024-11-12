package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.mapper.GroupMemberMapper;
import com.bx.implatform.service.GroupMemberService;
import com.bx.implatform.util.DateTimeUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP_MEMBER_ID)
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements GroupMemberService {
    @CacheEvict(key = "#member.getGroupId()")
    @Override
    public boolean save(GroupMember member) {
        return super.save(member);
    }

    @CacheEvict(key = "#groupId")
    @Override
    public boolean saveOrUpdateBatch(Long groupId, List<GroupMember> members) {
        return super.saveOrUpdateBatch(members);
    }

    @Override
    public GroupMember findByGroupAndUserId(Long groupId, Long userId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        return this.getOne(wrapper);
    }

    @Override
    public List<GroupMember> findByUserId(Long userId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getUserId, userId).eq(GroupMember::getQuit, false);
        return this.list(memberWrapper);
    }

    @Override
    public List<GroupMember> findQuitInMonth(Long userId) {
        Date monthTime = DateTimeUtils.addMonths(new Date(), -1);
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getUserId, userId).eq(GroupMember::getQuit, true)
            .ge(GroupMember::getQuitTime, monthTime);
        return this.list(memberWrapper);
    }

    @Override
    public List<GroupMember> findByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId);
        return this.list(memberWrapper);
    }

    @Cacheable(key = "#groupId")
    @Override
    public List<Long> findUserIdsByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getQuit, false)
            .select(GroupMember::getUserId);
        List<GroupMember> members = this.list(memberWrapper);
        return members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
    }

    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupId(Long groupId) {
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId, groupId).set(GroupMember::getQuit, true)
            .set(GroupMember::getQuitTime, new Date());
        this.update(wrapper);
    }

    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupAndUserId(Long groupId, Long userId) {
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId).set(GroupMember::getQuit, true)
            .set(GroupMember::getQuitTime, new Date());
        this.update(wrapper);
    }

    @Override
    public Boolean isInGroup(Long groupId, List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return true;
        }
        LambdaQueryWrapper<GroupMember> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getQuit, false)
            .in(GroupMember::getUserId, userIds);
        return userIds.size() == this.count(wrapper);
    }
}
