<template>
  <el-container class="chat-page">
    <resizable-aside :default-width="260" :min-width="200" :max-width="500" storage-key="chat-aside-width">
      <div class="header">
        <el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
        <el-button plain class="add-btn" icon="el-icon-plus" title="更多" @click="onClickAddMenu($event)"></el-button>
      </div>
      <div class="chat-loading" v-if="loading" v-loading="true" element-loading-text="消息接收中..."
        element-loading-spinner="el-icon-loading" element-loading-background="#F9F9F9" element-loading-size="24">
      </div>
      <virtual-scroller class="scroll-box" :items="showConversations">
        <template v-slot="{ item }">
          <chat-item :conversation="item" @click.native="onActiveItem(item)" @delete="onDelItem(item)"
            @info="onShowInfo(item)" @dnd="onDnd(item)" @top="onTop(item)" :active="item === activeConv"></chat-item>
        </template>
      </virtual-scroller>
    </resizable-aside>
    <el-container>
      <chat-box v-if="activeConv" :conversation="activeConv"></chat-box>
    </el-container>
    <add-friend :dialogVisible="showAddFriend" @close="onCloseAddFriend"></add-friend>
    <group-member-invite ref="groupMemberInvite" @success="onCreateGroupSuccess"></group-member-invite>
    <right-menu ref="rightMenu" @select="onSelectMenu"></right-menu>
  </el-container>
</template>

<script>
import ChatItem from "../components/chat/ChatItem.vue";
import ChatBox from "../components/chat/ChatBox.vue";
import AddFriend from "../components/friend/AddFriend.vue";
import GroupMemberInvite from "../components/group/GroupMemberInvite.vue";
import RightMenu from "../components/common/RightMenu.vue";
import ResizableAside from "../components/common/ResizableAside.vue";
import VirtualScroller from "../components/common/VirtualScroller.vue";

export default {
  name: "chat",
  components: {
    ChatItem,
    ChatBox,
    AddFriend,
    GroupMemberInvite,
    RightMenu,
    ResizableAside,
    VirtualScroller
  },
  data() {
    return {
      searchText: "",
      messageContent: "",
      group: {},
      groupMembers: [],
      showAddFriend: false
    }
  },
  methods: {
    onClickAddMenu(e) {
      this.$refs.rightMenu.open(e, this.addMenuItems);
    },
    onSelectMenu(item) {
      switch (item.key) {
        case 'ADD_FRIEND':
          this.showAddFriend = true;
          break;
        case 'CREATE_GROUP':
          this.$refs.groupMemberInvite.openCreate();
          break;
      }
    },
    onCloseAddFriend() {
      this.showAddFriend = false;
    },
    async onCreateGroupSuccess(group) {
      await this.groupStore.addGroup(group);
      await this.groupStore.refreshMember(group.id);
      const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, group.id);
      const chatInfo = {
        key: convKey,
        type: this.$enums.CONVERSATION_TYPE.GROUP,
        targetId: group.id,
        showName: group.showGroupName,
        headImage: group.headImageThumb,
        isDnd: group.isDnd
      };
      await this.chatStore.openChat(chatInfo);
      await this.chatStore.moveTop(convKey);
      this.chatStore.setActive(convKey);
    },
    onActiveItem(conv) {
      this.chatStore.setActive(conv.key);
    },
    onDelItem(conv) {
      const tip = `删除后记录将清空,确认删除与'${conv.showName}'的聊天 ?`;
      this.$confirm(tip, '删除聊天', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        if (this.isPrivate(conv) || this.isGroup(conv)) {
          const data = { chatId: conv.targetId }
          const chatTypeText = this.isPrivate(conv) ? "private" : "group";
          await this.$http({
            url: `/message/${chatTypeText}/deleteChat`,
            method: 'delete',
            data: data
          });
        }
        await this.chatStore.remove(conv.key);
      });
    },
    onShowInfo(conv) {
      if (this.isPrivate(conv)) {
        this.$router.push("/home/friend?id=" + conv.targetId);
      } else if (this.isGroup(conv)) {
        if (!this.groupStore.isGroup(conv.targetId)) {
          this.$message.error("您已不在群聊中")
          return;
        }
        this.$router.push("/home/group?id=" + conv.targetId);
      }
    },
    onTop(conv) {
      this.chatStore.setTop(conv.key, !conv.isTop)
    },
    onDnd(conv) {
      if (this.isPrivate(conv)) {
        this.setFriendDnd(conv, conv.targetId, !conv.isDnd)
      } else {
        this.setGroupDnd(conv, conv.targetId, !conv.isDnd)
      }
    },
    setFriendDnd(conv, friendId, isDnd) {
      const formData = {
        friendId: friendId,
        isDnd: isDnd
      }
      this.$http({
        url: '/friend/dnd',
        method: 'put',
        data: formData
      }).then(() => {
        this.friendStore.setDnd(friendId, isDnd)
        this.chatStore.setDnd(conv.key, isDnd)
      })
    },
    setGroupDnd(conv, groupId, isDnd) {
      const formData = {
        groupId: groupId,
        isDnd: isDnd
      }
      this.$http({
        url: '/group/dnd',
        method: 'put',
        data: formData
      }).then(() => {
        this.groupStore.setDnd(groupId, isDnd)
        this.chatStore.setDnd(conv.key, isDnd)
      })
    },
    isShow(conv) {
      return !this.searchText || conv.showName.includes(this.searchText)
    },
    isPrivate(conv) {
      return this.$enums.CONVERSATION_TYPE.PRIVATE == conv.type
    },
    isGroup(conv) {
      return this.$enums.CONVERSATION_TYPE.GROUP == conv.type
    },
  },
  computed: {
    activeConv() {
      return this.chatStore.activeConversation;
    },
    loading() {
      return this.chatStore.loading;
    },
    showConversations() {
      return this.chatStore.conversations.filter(conv => this.isShow(conv));
    },
    addMenuItems() {
      return [
        {
          key: 'ADD_FRIEND',
          name: '添加好友',
          icon: 'el-icon-user'
        },
        {
          key: 'CREATE_GROUP',
          name: '发起群聊',
          icon: 'el-icon-chat-dot-round'
        }
      ];
    }
  }
}
</script>

<style lang="scss">
.chat-page {

  .header {
    height: 60px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    padding: 0 12px;
    box-sizing: border-box;

    .search-text {
      flex: 1;
    }

    .add-btn {
      padding: 8px;
      margin: 5px;
      font-size: 16px;
      border-radius: 50%;
      background: var(--im-background-active);
      color: var(--im-color-primary);
      transition: all 0.3s ease;
      font-weight: 600;
      border: var(--im-border);

      &:hover {
        background: var(--im-background-active-dark);
        transform: scale(1.05);
      }
    }
  }

  .chat-loading {
    height: 50px;
    background-color: #eee;

    .el-icon-loading {
      font-size: 24px;
      color: var(--im-text-color-light);
    }

    .el-loading-text {
      color: var(--im-text-color-light);
    }
  }

  .chat-items {
    flex: 1;
  }

}
</style>
