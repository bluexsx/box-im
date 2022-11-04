package com.lx.implatform.service;

import com.lx.implatform.entity.Group;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.implatform.vo.GroupInviteVO;
import com.lx.implatform.vo.GroupMemberVO;
import com.lx.implatform.vo.GroupVO;

import java.util.List;

/**
 * <p>
 * 群 服务类
 * </p>
 *
 * @author blue
 * @since 2022-10-31
 */
public interface IGroupService extends IService<Group> {


    GroupVO createGroup(String groupName);

    GroupVO modifyGroup(GroupVO vo);

    void deleteGroup(Long groupId);

    void quitGroup(Long groupId);

    void kickGroup(Long groupId,Long userId);

    List<GroupVO>  findGroups();

    void invite(GroupInviteVO vo);

    Group GetById(Long groupId);

    GroupVO findById(Long groupId);

    List<GroupMemberVO> findGroupMembers(Long groupId);
}
