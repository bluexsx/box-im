package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.dto.GroupInviteDTO;
import com.bx.implatform.dto.GroupMemberRemoveDTO;
import com.bx.implatform.entity.Group;
import com.bx.implatform.vo.GroupMemberVO;
import com.bx.implatform.vo.GroupVO;

import java.util.List;

public interface GroupService extends IService<Group> {

    /**
     * 创建新群聊
     *
     * @param vo 群聊信息
     * @return 群聊信息
     **/
    GroupVO createGroup(GroupVO vo);

    /**
     * 修改群聊信息
     *
     * @param vo 群聊信息
     * @return 群聊信息
     **/
    GroupVO modifyGroup(GroupVO vo);

    /**
     * 删除群聊
     *
     * @param groupId 群聊id
     **/
    void deleteGroup(Long groupId);

    /**
     * 退出群聊
     *
     * @param groupId 群聊id
     */
    void quitGroup(Long groupId);

    /**
     * 将用户踢出群聊
     *
     * @param groupId 群聊id
     * @param userId  用户id
     */
    void kickGroup(Long groupId, Long userId);

    /**
     * 将用户移出群聊
     * @param dto dto
     */
    void removeGroupMembers(GroupMemberRemoveDTO dto);

    /**
     * 查询当前用户的所有群聊
     *
     * @return 群聊信息列表
     **/
    List<GroupVO> findGroups();

    /**
     * 邀请好友进群
     *
     * @param dto 群id、好友id列表
     **/
    void invite(GroupInviteDTO dto);

    /**
     * 根据id查找群聊，并进行缓存
     *
     * @param groupId 群聊id
     * @return 群聊实体
     */
    Group getAndCheckById(Long groupId);

    /**
     * 根据id查找群聊
     *
     * @param groupId 群聊id
     * @return 群聊vo
     */
    GroupVO findById(Long groupId);

    /**
     * 查询群成员
     *
     * @param groupId 群聊id
     * @return List<GroupMemberVO>
     **/
    List<GroupMemberVO> findGroupMembers(Long groupId);
}
