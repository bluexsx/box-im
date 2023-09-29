import {USER_STATE} from "../common/enums"
import request from '../common/request'


export default {
	
	state: {
		userInfo: {},
		state: USER_STATE.FREE 
	},

	mutations: {
		setUserInfo(state, userInfo) {
			// 切换用户后，清理缓存
			if(userInfo.id != state.userInfo.id){
				console.log("用户切换")
				this.commit("resetChatStore");
			}
			state.userInfo = userInfo;
		},
		setUserState(state, userState) {
			state.state = userState;
		},
	},
	actions:{
		initUserStore(context){
			return new Promise((resolve, reject) => {
				request({
					url: '/user/self',
					method: 'GET'
				}).then((userInfo) => {
					context.commit("setUserInfo",userInfo);
					resolve();
					console.log("suerstore")
				}).catch(()=>{
					reject();
				});
			})
		}
	}
	
}