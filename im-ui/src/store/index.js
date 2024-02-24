import Vue from 'vue';
import Vuex from 'vuex';
import chatStore from './chatStore.js';
import friendStore from './friendStore.js';
import userStore from './userStore.js';
import groupStore from './groupStore.js';
import uiStore from './uiStore.js';

Vue.use(Vuex)

export default new Vuex.Store({
	modules: {chatStore,friendStore,userStore,groupStore,uiStore},
	state: {},
	mutations: {
	},
	actions: {
		load(context) {
			return this.dispatch("loadUser").then(() => {
				const promises = [];
				promises.push(this.dispatch("loadFriend"));
				promises.push(this.dispatch("loadGroup"));
				promises.push(this.dispatch("loadChat"));
				return Promise.all(promises);
			})
		},
		unload(context){
			context.commit("clear");
		}
	},
	strict: process.env.NODE_ENV !== 'production'
})
