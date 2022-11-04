import Vue from 'vue';
import Vuex from 'vuex';
import chatStore from './chatStore.js';
import friendStore from './friendStore.js';
import userStore from './userStore.js';
import groupStore from './groupStore.js';
import uiStore from './uiStore.js';
import VuexPersistence from 'vuex-persist'


const vuexLocal = new VuexPersistence({
    storage: window.localStorage,
	modules: ["userStore","chatStore"]
})

Vue.use(Vuex)

export default new Vuex.Store({
	modules: {chatStore,friendStore,userStore,groupStore,uiStore},
	state: {},
	plugins: [vuexLocal.plugin],
	mutations: {
		initStore(state){
			this.commit("initFriendStore");
			this.commit("initGroupStore");
			this.commit("initChatStore");
		}
		
	},
	strict: process.env.NODE_ENV !== 'production'
})
