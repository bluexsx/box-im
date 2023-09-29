
import chatStore from './chatStore.js';
import friendStore from './friendStore.js';
import userStore from './userStore.js';
import groupStore from './groupStore.js';
import uiStore from './uiStore.js';

// #ifndef VUE3
import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)
const store = new Vuex.Store({
// #endif

// #ifdef VUE3
import { createStore } from 'vuex';
  const store = createStore({
// #endif
	modules: {chatStore,friendStore,userStore,groupStore,uiStore},
	state: {},
	actions: {
		initStore(context){
			const promises = [];
			promises.push(this.dispatch("initUserStore"));
			promises.push(this.dispatch("initFriendStore"));
			promises.push(this.dispatch("initGroupStore"));
			return Promise.all(promises);
		}
	},
	strict: true
});

export default store;
