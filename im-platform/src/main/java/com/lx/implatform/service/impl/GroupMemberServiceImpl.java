package com.lx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lx.implatform.entity.GroupMember;
import com.lx.implatform.mapper.GroupMemberMapper;
import com.lx.implatform.service.IGroupMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 群成员 服务实现类
 * </p>
 *
 * @author blue
 * @since 2022-10-31
 */
@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements IGroupMemberService {


    /**
     * 根据群聊id和用户id查询群聊成员
     *
     * @param groupId 群聊id
     * @param userId 用户id
     * @return
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
     * @return
     */
    @Override
    public List<GroupMember> findByUserId(Long userId) {
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
    public List<GroupMember> findByGroupId(Long groupId) {
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
    @Override
    public void removeByGroupId(long groupId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper();
        wrapper.lambda().eq(GroupMember::getGroupId,groupId);
        this.remove(wrapper);
    }
}
