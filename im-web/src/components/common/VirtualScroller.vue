<template>
    <el-scrollbar ref="scrollbar">
        <div v-for="(item, idx) in items" :key="idx">
            <slot :item="item" v-if=" idx < showMaxIdx">
            </slot>
        </div>
    </el-scrollbar>
</template>

<script>
export default {
    name: "virtualScroller",
    data() {
        return {
            page: 1,
            isInitEvent: false,
            lockTip: false
        }
    },
    props: {
        items: {
            type: Array
        },
        size: {
            type: Number,
            default: 30
        }
    },
    methods: {
        init() {
            this.page = 1;
            this.initEvent();
        },
        initEvent() {
            if (!this.isInitEvent) {
                let scrollWrap = this.$refs.scrollbar.$el.querySelector('.el-scrollbar__wrap');
                scrollWrap.addEventListener('scroll', this.onScroll);
                this.isInitEvent = true;
            }
        },
        onScroll(e) {
            const scrollbar = e.target;
            // 滚到底部
            if (scrollbar.scrollTop + scrollbar.clientHeight >= scrollbar.scrollHeight - 30) {
                if(this.showMaxIdx >= this.items.length ){
                    this.showTip();
                }else{
                    this.page++;
                }
            }
        },
        showTip(){
            // 提示限制最多3秒显示一次
            if(!this.lockTip){
                this.$message.success("已到滚动到底部")
                this.lockTip = true;
                setTimeout(()=>{
                    this.lockTip = false;
                },3000)
            }
        }
    },
    computed: {
        showMaxIdx() {
            return Math.min(this.page * this.size, this.items.length);
        }
    },
    mounted(){
        this.initEvent();
    }
}
</script>

<style scoped></style>
