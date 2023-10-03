import {USER_STATE} from "../common/enums"
import request from '../common/request'


export default {
	
	state: {
		userInfo: {},
		state: USER_STATE.FREE 
	},

	mutations: {
		setUserInfo(state, userInfo) {
			state.userInfo = userInfo;
		},
		setUserState(state, userState) {
			state.state = userState;
		},
	},
	actions:{
		loadUser(context){
			return new Promise((resolve, reject) => {
				request({
					url: '/user/self',
					method: 'GET'
				}).then((userInfo) => {
					context.commit("setUserInfo",userInfo);
					resolve();
				}).catch(()=>{
					reject();
				});
			})
		}
	}
	
}