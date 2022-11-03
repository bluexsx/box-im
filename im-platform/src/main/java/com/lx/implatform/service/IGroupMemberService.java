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



    GroupMember findByGroupAndUserId(Long groupId,Long userId);

    List<GroupMember>  findByUserId(Long userId);

    List<GroupMember>  findByGroupId(Long groupId);

    List<Long> findUserIdsByGroupId(Long groupId);

    boolean save(GroupMember member);

    boolean saveOrUpdateBatch(Long groupId,List<GroupMember> members);

    void removeByGroupId(Long groupId);

    void removeByGroupAndUserId(Long groupId,Long userId);
}
