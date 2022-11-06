package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.Group;
import com.bx.implatform.vo.GroupInviteVO;
import com.bx.implatform.vo.GroupMemberVO;
import com.bx.implatform.vo.GroupVO;

import java.util.List;


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
