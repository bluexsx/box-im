import httpRequest from '../api/httpRequest.js'

export default {

	state: {
		groups: [],
		activeIndex: -1,
	},
	mutations: {
		initGroupStore(state) {
			httpRequest({
				url: '/api/group/list',
				method: 'get'
			}).then((groups) => {
				this.commit("setGroups",groups);
			})
		},
		setGroups(state,groups){
			state.groups = groups;
		},
		activeGroup(state,index){
			state.activeIndex = index;
		}
	}	
}