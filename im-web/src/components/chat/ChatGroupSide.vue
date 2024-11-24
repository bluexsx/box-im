<template>
  <div class="chat-group-side">
    <div v-show="!group.quit" class="group-side-search">
      <el-input placeholder="搜索群成员" v-model="searchText" size="small">
        <i class="el-icon-search el-input__icon" slot="prefix"> </i>
      </el-input>
    </div>
    <div class="group-side-scrollbar">
      <div v-show="!group.quit" class="group-side-member-list">
        <div class="group-side-invite">
          <div class="invite-member-btn" title="邀请好友进群聊" @click="showAddGroupMember = true">
            <i class="el-icon-plus"></i>
          </div>
          <div class="invite-member-text">邀请</div>
          <add-group-member :visible="showAddGroupMember" :groupId="group.id" :members="groupMembers"
            @reload="$emit('reload')" @close="showAddGroupMember = false"></add-group-member>
        </div>
        <div v-for="(member) in groupMembers" :key="member.id">
          <group-member class="group-side-member" v-show="!member.quit && member.showNickName.includes(searchText)"
            :member="member" :showDel="false"></group-member>
        </div>
      </div>
      <el-divider v-if="!group.quit" content-position="center"></el-divider>
      <el-form labelPosition="top" class="group-side-form" :model="group" size="small">
        <el-form-item label="群聊名称">
          <el-input v-model="group.name" disabled maxlength="20"></el-input>
        </el-form-item>
        <el-form-item label="群主">
          <el-input :value="ownerName" disabled></el-input>
        </el-form-item>
        <el-form-item label="群公告">
          <el-input v-model="group.notice" disabled type="textarea" maxlength="1024"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="group.remarkGroupName" :disabled="!editing" maxlength="20"></el-input>
        </el-form-item>
        <el-form-item label="我在本群的昵称">
          <el-input v-model="group.remarkNickName" :disabled="!editing" maxlength="20"></el-input>
        </el-form-item>
        <div v-show="!group.quit" class="btn-group">
          <el-button v-if="editing" type="success" @click="onSaveGroup()">保存</el-button>
          <el-button v-if="!editing" type="primary" @click="editing = !editing">编辑</el-button>
          <el-button type="danger" v-show="!isOwner" @click="onQuit()">退出群聊</el-button>
        </div>
      </el-form>
    </div>

  </div>
</template>

<script>
import AddGroupMember from '../group/AddGroupMember.vue';
import GroupMember from '../group/GroupMember.vue';

export default {
  name: "chatGroupSide",
  components: {
    AddGroupMember,
    GroupMember
  },
  data() {
    return {
      searchText: "",
      editing: false,
      showAddGroupMember: false
    }
  },
  props: {
    group: {
      type: Object
    },
    groupMembers: {
      type: Array
    }
  },
  methods: {
    onClose() {
      this.$emit('close');
    },
    loadGroupMembers() {
      this.$http({
        url: `/group/members/${this.group.id}`,
        method: "get"
      }).then((members) => {
        this.groupMembers = members;
      })
    },
    onSaveGroup() {
      let vo = this.group;
      this.$http({
        url: "/group/modify",
        method: "put",
        data: vo
      }).then((group) => {
        this.editing = !this.editing
        this.$store.commit("updateGroup", group);
        this.$emit('reload');
        this.$message.success("修改成功");
      })
    },
    onQuit() {
      this.$confirm('退出群聊后将不再接受群里的消息，确认退出吗？', '确认退出?', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: `/group/quit/${this.group.id}`,
          method: 'delete'
        }).then(() => {
          this.$store.commit("removeGroup", this.group.id);
          this.$store.commit("activeGroup", -1);
          this.$store.commit("removeGroupChat", this.group.id);
        });
      })
    },

  },
  computed: {
    ownerName() {
      let member = this.groupMembers.find((m) => m.userId == this.group.ownerId);
      return member && member.showNickName;
    },
    isOwner() {
      return this.group.ownerId == this.$store.state.userStore.userInfo.id;
    }

  }
}
</script>

<style lang="scss">
.chat-group-side {
  position: relative;

  .group-side-search {
    padding: 10px;
  }

  .group-side-scrollbar {
    overflow: auto;
  }

  .el-divider--horizontal {
    margin: 0;
  }

  .el-form-item {
    margin-bottom: 0px !important;
  }

  .group-side-member-list {
    padding: 10px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    font-size: 14px;
    text-align: center;

    .group-side-member {
      margin-left: 5px;
    }

    .group-side-invite {
      display: flex;
      flex-direction: column;
      align-items: center;
      width: 50px;
      margin-left: 5px;

      .invite-member-btn {
        width: 38px;
        height: 38px;
        line-height: 38px;
        border: var(--im-border);
        font-size: 14px;
        cursor: pointer;
        box-sizing: border-box;

        &:hover {
          border: #aaaaaa solid 1px;
        }
      }

      .invite-member-text {
        font-size: 12px;
        text-align: center;
        width: 100%;
        height: 30px;
        line-height: 30px;
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden
      }
    }
  }

  .group-side-form {
    text-align: left;
    padding: 10px;
    height: 30%;

    .el-form-item {
      margin-bottom: 12px;

      .el-form-item__label {
        padding: 0;
        line-height: 30px;
      }

      .el-textarea__inner {
        min-height: 100px !important;
      }
    }

    .el-input__inner,
    .el-textarea__inner {
      color: var(--im-text-color) !important;
    }


    .btn-group {
      text-align: center;
      margin-top: 12px;
    }
  }

}
</style>
