import httpRequest from '../api/httpRequest.js'

export default {

	state: {
		friendsList: [],
		activeIndex: -1,
		timer: null
	},
	mutations: {
		initFriendsStore(state) {
			httpRequest({
				url: '/api/friends/list',
				method: 'get'
			}).then((friendsList) => {
				this.commit("setFriendsList",friendsList);
				this.commit("refreshOnlineStatus");
			})
		},

		setFriendsList(state, friendsList) {
			state.friendsList = friendsList;
		},
		updateFriends(state,friendsInfo){
			console.log(friendsInfo)
			state.friendsList.forEach((f,index)=>{
				if(f.friendId==friendsInfo.friendId){
					state.friendsList[index] = friendsInfo;
				}
			})
		},
		activeFriends(state, index) {
			state.activeIndex = index;
		},
		removeFriends(state, index) {
			state.friendsList.splice(index, 1);
		},
		addFriends(state, friendsInfo) {
			state.friendsList.push(friendsInfo);
		},
		refreshOnlineStatus(state){
			let userIds = [];
			if(state.friendsList.length ==0){
				return; 
			}
			state.friendsList.forEach((f)=>{userIds.push(f.friendId)});
			httpRequest({
				url: '/api/user/online',
				method: 'get',
				params: {userIds: userIds.join(',')}
			}).then((onlineIds) => {
				this.commit("setOnlineStatus",onlineIds);
			})
			
			// 30s后重新拉取
			clearTimeout(state.timer);
			state.timer = setTimeout(()=>{
				this.commit("refreshOnlineStatus");
			},30000)
		},
		setOnlineStatus(state,onlineIds){
			state.friendsList.forEach((f)=>{
				let onlineFriend = onlineIds.find((id)=> f.friendId==id);
				f.online = onlineFriend != undefined;
				console.log(f.friendNickName+":"+f.online);
			});
			
			let activeFriend = state.friendsList[state.activeIndex];
			state.friendsList.sort((f1,f2)=>{
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
				state.friendsList.forEach((f,i)=>{
					if(f.friendId == activeFriend.friendId){
						state.activeIndex = i;
					}
				})
			}
		}
		
	}
}
