import chatStore from './chatStore.js';
import friendStore from './friendStore.js';
import userStore from './userStore.js';
import groupStore from './groupStore.js';
import {
	createStore
} from 'vuex';
const store = createStore({
	modules: {
		chatStore,
		friendStore,
		userStore,
		groupStore
	},
	state: {},
	actions: {
		load(context) {
			return this.dispatch("loadUser").then(() => {
				const promises = [];
				promises.push(this.dispatch("loadFriend"));
				promises.push(this.dispatch("loadGroup"));
				promises.push(this.dispatch("loadChat"));
				return Promise.all(promises);
			})

		}
	},
	strict: true
})

export default store;