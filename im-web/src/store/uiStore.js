export default {
	state: {
		userInfo: { // 用户信息窗口
			show: false,
			user: {},
			pos: {
				x: 0,
				y: 0
			}
		},
		fullImage: { // 全屏大图
			show: false,
			url: ""
		}
	},
	mutations: {
		showUserInfoBox(state, user) {
			state.userInfo.show = true;
			state.userInfo.user = user;

		},
		setUserInfoBoxPos(state, pos) {
			let w = document.documentElement.clientWidth;
			let h = document.documentElement.clientHeight;
			state.userInfo.pos.x = Math.min(pos.x, w - 350);
			state.userInfo.pos.y = Math.min(pos.y, h - 200);
		},
		closeUserInfoBox(state) {
			state.userInfo.show = false;
		},
		showFullImageBox(state, url) {
			state.fullImage.show = true;
			state.fullImage.url = url;
		},
		closeFullImageBox(state) {
			state.fullImage.show = false;
		}
	}
}