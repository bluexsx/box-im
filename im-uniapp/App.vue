<script>
	export default {
		onLaunch() {
			// 跳转到登录页面
				console.log("onLaunch")
				// #ifdef H5
				// 小程序不能跳转，否则会触发多次初始化
				uni.navigateTo({
					url:"/pages/login/login" 
				})
				// #endif
		}
	}
</script>

<style lang="scss">
	@import url('./static/icon/iconfont.css');
</style>