package com.lx.implatform.service;

import com.lx.implatform.entity.GroupMember;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 群成员 服务类
 * </p>
 *
 * @author blue
 * @since 2022-10-31
 */
public interface IGroupMemberService extends IService<GroupMember> {



    GroupMember findByGroupAndUserId(long groupId,long userId);

    List<GroupMember>  findByUserId(long userId);

    List<GroupMember>  findByGroupId(long groupId);

    boolean save(GroupMember member);

    boolean saveBatch(long groupId,List<GroupMember> members);

    void removeByGroupId(long groupId);

    void removeByGroupAndUserId(long groupId,long userId);
}
