<template>
  <el-container class="chat-page">
    <el-aside width="260px" class="chat-list-box">
      <div class="chat-list-header">
        <el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
      </div>
      <div class="chat-list-loading" v-if="loading" v-loading="true" element-loading-text="消息接收中..."
        element-loading-spinner="el-icon-loading" element-loading-background="#F9F9F9" element-loading-size="24">
      </div>
      <el-scrollbar class="chat-list-items" v-else>
        <div v-for="(chat, index) in chatStore.chats" :key="index">
          <chat-item v-show="!chat.delete && chat.showName && chat.showName.includes(searchText)" :chat="chat" :index="index"
            @click.native="onActiveItem(index)" @delete="onDelItem(index)" @top="onTop(index)"
            :active="chat === chatStore.activeChat"></chat-item>
        </div>
      </el-scrollbar>
    </el-aside>
    <el-container class="chat-box">
      <chat-box v-if="chatStore.activeChat" :chat="chatStore.activeChat"></chat-box>
    </el-container>
  </el-container>
</template>

<script>
import ChatItem from "../components/chat/ChatItem.vue";
import ChatBox from "../components/chat/ChatBox.vue";

export default {
  name: "chat",
  components: {
    ChatItem,
    ChatBox
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
      this.$store.commit("activeChat", index);
    },
    onDelItem(index) {
      this.$store.commit("removeChat", index);
    },
    onTop(chatIdx) {
      this.$store.commit("moveTop", chatIdx);
    },
  },
  computed: {
    chatStore() {
      return this.$store.state.chatStore;
    },
    loading() {
      return this.chatStore.loadingGroupMsg || this.chatStore.loadingPrivateMsg
    }
  }
}
</script>

<style lang="scss">
.chat-page {
  .chat-list-box {
    display: flex;
    flex-direction: column;
    background: var(--im-background);

    .chat-list-header {
      height: 50px;
      display: flex;
      align-items: center;
      padding: 0 8px;
    }

    .chat-list-loading {
      height: 50px;
      background-color: #eee;

      .el-icon-loading {
        font-size: 24px;
        color: var(--im-text-color-light);
      }

      .el-loading-text {
        color: var(--im-text-color-light);
      }

      .chat-loading-box {
        height: 100%;
      }
    }

    .chat-list-items {
      flex: 1;
    }
  }
}
</style>
