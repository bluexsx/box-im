import httpRequest from '../api/httpRequest.js'
import {TERMINAL_TYPE} from "../api/enums.js"

export default {

	state: {
		friends: [],
		activeIndex: -1,
		timer: null
	},
	mutations: {
		initFriendStore(state) {
			httpRequest({
				url: '/friend/list',
				method: 'get'
			}).then((friends) => {
				this.commit("setFriends",friends);
				this.commit("refreshOnlineStatus");
			})
		},

		setFriends(state, friends) {
			state.friends = friends;
		},
		updateFriend(state,friend){
			state.friends.forEach((f,index)=>{
				if(f.id==friend.id){
					// 拷贝属性
					let online = state.friends[index].online;
					Object.assign(state.friends[index], friend);
					state.friends[index].online =online;
				}
			})
		},
		activeFriend(state, index) {
			state.activeIndex = index;
		},
		removeFriend(state, index) {
			state.friends.splice(index, 1);
			if(state.activeIndex  >= state.friends.length){
				state.activeIndex = state.friends.length-1;
			}
		},
		addFriend(state, friend) {
			state.friends.push(friend);
		},
		refreshOnlineStatus(state){
			let userIds = [];
			console.log("refreshOnlineStatus")
			if(state.friends.length ==0){
				return; 
			}
			state.friends.forEach((f)=>{userIds.push(f.id)});
			httpRequest({
				url: '/user/terminal/online',
				method: 'get',
				params: {userIds: userIds.join(',')}
			}).then((onlineTerminals) => {
				this.commit("setOnlineStatus",onlineTerminals);
			})
			
			// 30s后重新拉取
			clearTimeout(state.timer);
			state.timer = setTimeout(()=>{
				this.commit("refreshOnlineStatus");
			},30000)
		},
		setOnlineStatus(state,onlineTerminals){
			state.friends.forEach((f)=>{
				let userTerminal = onlineTerminals.find((o)=> f.id==o.userId);
				if(userTerminal){
					console.log(userTerminal)
					f.online = true;
					f.onlineTerminals = userTerminal.terminals;
					f.onlineWeb = userTerminal.terminals.indexOf(TERMINAL_TYPE.WEB)>=0
					f.onlineApp = userTerminal.terminals.indexOf(TERMINAL_TYPE.APP)>=0
				}else{
					f.online = false;
					f.onlineTerminals = [];
					f.onlineWeb = false;
					f.onlineApp = false;
				}
			});
			let activeFriend = state.friends[state.activeIndex];
			state.friends.sort((f1,f2)=>{
				if(f1.online&&!f2.online){
					return -1;
				}
				if(f2.online&&!f1.online){
					return 1;
				}
				return 0;
			});
			
			// 重新排序后，activeIndex指向的好友可能会变化，需要重新指定
			if(state.activeIndex >=0){
				state.friends.forEach((f,i)=>{
					if(f.id == activeFriend.id){
						state.activeIndex = i;
					}
				})
			}
		}
		
	}
}
