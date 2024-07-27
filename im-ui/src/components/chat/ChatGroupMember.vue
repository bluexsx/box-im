<template>
    <div class="chat-group-member" :style="{'height':height+'px'}">
        <div class="member-avatar">
            <head-image :size="headImageSize" :name="member.showNickName" :url="member.headImage"> </head-image>
        </div>
        <div class="member-name" :style="{'line-height':height+'px'}">
            <div>{{ member.showNickName }}</div>
        </div>
    </div>
</template>

<script>
import HeadImage from "../common/HeadImage.vue";
export default {
    name: "groupMember",
    components: { HeadImage },
    data() {
        return {};
    },
    props: {
        member: {
            type: Object,
            required: true
        },
        height:{
            type: Number,
            default: 50
        }
    },
    computed:{
        headImageSize(){
            return Math.ceil(this.height * 0.75)
        }
    }
}
</script>

<style lang="scss">
.chat-group-member {
    display: flex;
    margin-bottom: 1px;
    position: relative;
    padding: 0 5px;
    align-items: center;
    background-color: #fafafa;
    white-space: nowrap;
    box-sizing: border-box;

    &:hover {
        background-color: #eeeeee;
    }

    &.active {
        background-color: #eeeeee;
    }

    .member-name {
        padding-left: 10px;
        height: 100%;
        text-align: left;
        white-space: nowrap;
        overflow: hidden;
        font-size: 14px;
        font-weight: 600;
    }
}
</style>
