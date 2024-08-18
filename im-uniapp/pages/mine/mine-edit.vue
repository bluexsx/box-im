<template>
	<view class="page mine-edit">
		<uni-forms ref="form" :modelValue="userInfo" label-position="top"
			label-width="100%">
			<uni-forms-item label="头像:" name="headImage">
				<image-upload :onSuccess="onUnloadImageSuccess">
					<image :src="userInfo.headImageThumb" class="head-image"></image>
				</image-upload>
			</uni-forms-item>
			<uni-forms-item label="用户名:" name="userName">
				<uni-easyinput type="text" v-model="userInfo.userName" :disabled="true" />
			</uni-forms-item>
			<uni-forms-item label="昵称:" name="nickName">
				<uni-easyinput v-model="userInfo.nickName" type="text" :placeholder="userInfo.userName" />
			</uni-forms-item>
			<uni-forms-item label="性别:" name="sex">
				<radio-group @change="onSexchange">
					<label class="radio"><radio value="0" :checked="userInfo.sex==0" />男</label>
					<label class="radio"><radio value="1" :checked="userInfo.sex==1" />女  </label>
				</radio-group>
			</uni-forms-item>
			<uni-forms-item label="签名:" name="signature">
				<uni-easyinput type="textarea" v-model="userInfo.signature" placeholder="编辑个性标签,展示我的独特态度" />
			</uni-forms-item>
		</uni-forms>
		<button type="primary" @click="onSubmit()">提交</button>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				userInfo: {}
			}
		},
		methods:{
			onSexchange(e){
				this.userInfo.sex=e.detail.value;
			},
			onUnloadImageSuccess(file, res) {
				this.userInfo.headImage = res.data.originUrl;
				this.userInfo.headImageThumb = res.data.thumbUrl;
			},
			onSubmit(){
				this.$http({
					url: "/user/update",
					method: "PUT",
					data: this.userInfo
				}).then(()=>{
					this.userStore.setUserInfo(this.userInfo);
					uni.showToast({
						title:"修改成功",
						icon: 'none'
					});
					setTimeout(()=>{
						uni.navigateBack();
					},1000);
				})	
			}
		},
		onLoad() {
			// 深拷贝一份数据
			let mine = this.userStore.userInfo;
			this.userInfo = JSON.parse(JSON.stringify(mine));
		}
	}
</script>

<style scoped lang="scss">
	.mine-edit {
		padding: 20rpx;

		.head-image {
			width: 200rpx;
			height: 200rpx;
		}
	}
</style>