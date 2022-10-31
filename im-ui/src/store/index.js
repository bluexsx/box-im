import Vue from 'vue';
import Vuex from 'vuex';
import chatStore from './chatStore.js';
import friendStore from './friendStore.js';
import userStore from './userStore.js';
import VuexPersistence from 'vuex-persist'


const vuexLocal = new VuexPersistence({
    storage: window.localStorage,
	modules: ["userStore","chatStore"]
})

Vue.use(Vuex)

export default new Vuex.Store({
	modules: {chatStore,friendStore,userStore},
	state: {
		userInfo: {}
	},
	plugins: [vuexLocal.plugin],
	mutations: {
		initStore(state){
		
			this.commit("initFriendStore");
		}
		
	},
	strict: process.env.NODE_ENV !== 'production'
})
