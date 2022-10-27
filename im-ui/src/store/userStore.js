export default {
	
	state: {
		userInfo: {}
	},

	mutations: {
		setUserInfo(state, userInfo) {
			// 切换用户后，清理缓存
			if(userInfo.id != state.userInfo.id){
				console.log("用户切换")
				this.commit("resetChatStore");
			}
			state.userInfo = userInfo;
		}
	}
	
}