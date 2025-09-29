<template>
    <div class="group-member-item" :style="{ 'height': height + 'px' }">
        <div class="member-avatar">
            <head-image :size="headImageSize" :name="member.showNickName" :url="member.headImage"
                :online="member.online"> </head-image>
        </div>
        <div class="member-name" :style="{ 'line-height': height + 'px' }">
            <div>{{ member.showNickName }}</div>
        </div>
        <slot></slot>
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
        height: {
            type: Number,
            default: 50
        }
    },
    computed: {
        headImageSize() {
            return Math.ceil(this.height * 0.75)
        }
    }
}
</script>

<style lang="scss" scoped>
.group-member-item {
    display: flex;
    position: relative;
    padding: 0 15px;
    align-items: center;
    white-space: nowrap;
    box-sizing: border-box;
    border-radius: 5px;
    margin: 0 1px;

	&:hover {
		background-color: var(--im-background-active);
	}

	&.active {
		background-color: var(--im-background-active-dark);
	}

    .member-name {
        flex: 1;
        padding-left: 10px;
        height: 100%;
        text-align: left;
        white-space: nowrap;
        overflow: hidden;
        font-size: var(--im-font-size);
    }
}
</style>
