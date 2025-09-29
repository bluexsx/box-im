<template>
  <el-container class="chat-page">
    <resizable-aside :default-width="260" :min-width="200" :max-width="500" storage-key="chat-aside-width">
      <div class="header">
        <el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
      </div>
      <div class="chat-loading" v-if="loading" v-loading="true" element-loading-text="消息接收中..."
        element-loading-spinner="el-icon-loading" element-loading-background="#F9F9F9" element-loading-size="24">
      </div>
      <el-scrollbar class="chat-items" v-else>
        <div v-for="(chat, index) in chatStore.chats" :key="index">
          <chat-item v-show="!chat.delete && chat.showName && chat.showName.includes(searchText)" :chat="chat"
            :index="index" @click.native="onActiveItem(index)" @delete="onDelItem(index)" @top="onTop(index)"
            @dnd="onDnd(chat)" :active="chat === chatStore.activeChat"></chat-item>
        </div>
      </el-scrollbar>
    </resizable-aside>
    <el-container>
      <chat-box v-if="chatStore.activeChat" :chat="chatStore.activeChat"></chat-box>
    </el-container>
  </el-container>
</template>

<script>
import ChatItem from "../components/chat/ChatItem.vue";
import ChatBox from "../components/chat/ChatBox.vue";
import ResizableAside from "../components/common/ResizableAside.vue";

export default {
  name: "chat",
  components: {
    ChatItem,
    ChatBox,
    ResizableAside
  },
  data() {
    return {
      searchText: "",
      messageContent: "",
      group: {},
      groupMembers: []
    }
  },
  methods: {
    onActiveItem(index) {
      this.chatStore.setActiveChat(index);
    },
    onDelItem(index) {
      this.chatStore.removeChat(index);
    },
    onTop(chatIdx) {
      this.chatStore.moveTop(chatIdx);
    },
    onDnd(chat) {
      if (chat.type == 'PRIVATE') {
        this.setFriendDnd(chat, chat.targetId, !chat.isDnd)
      } else {
        this.setGroupDnd(chat, chat.targetId, !chat.isDnd)
      }
    },
    setFriendDnd(chat, friendId, isDnd) {
      let formData = {
        friendId: friendId,
        isDnd: isDnd
      }
      this.$http({
        url: '/friend/dnd',
        method: 'put',
        data: formData
      }).then(() => {
        this.friendStore.setDnd(friendId, isDnd)
        this.chatStore.setDnd(chat, isDnd)
      })
    },
    setGroupDnd(chat, groupId, isDnd) {
      let formData = {
        groupId: groupId,
        isDnd: isDnd
      }
      this.$http({
        url: '/group/dnd',
        method: 'put',
        data: formData
      }).then(() => {
        this.groupStore.setDnd(groupId, isDnd)
        this.chatStore.setDnd(chat, isDnd)
      })
    }
  },
  computed: {
    loading() {
      return this.chatStore.loading;
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-page {
  
  .header {
    height: 50px;
    display: flex;
    align-items: center;
    padding: 0 8px;
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
