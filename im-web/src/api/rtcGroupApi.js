import http from './httpRequest.js'

class RtcGroupApi { }

RtcGroupApi.prototype.setup = function (groupId, userInfos) {
	let formData = {
		groupId,
		userInfos
	}
	return http({
		url: '/webrtc/group/setup',
		method: 'post',
		data: formData
	})
}

RtcGroupApi.prototype.accept = function (groupId) {
	return http({
		url: '/webrtc/group/accept?groupId=' + groupId,
		method: 'post'
	})
}

RtcGroupApi.prototype.reject = function (groupId) {
	return http({
		url: '/webrtc/group/reject?groupId=' + groupId,
		method: 'post'
	})
}

RtcGroupApi.prototype.failed = function (groupId, reason) {
	let formData = {
		groupId,
		reason
	}
	return http({
		url: '/webrtc/group/failed',
		method: 'post',
		data: formData
	})
}


RtcGroupApi.prototype.join = function (groupId) {
	return http({
		url: '/webrtc/group/join?groupId=' + groupId,
		method: 'post'
	})
}

RtcGroupApi.prototype.invite = function (groupId, userInfos) {
	let formData = {
		groupId,
		userInfos
	}
	return http({
		url: '/webrtc/group/invite',
		method: 'post',
		data: formData
	})
}


RtcGroupApi.prototype.offer = function (groupId, userId, offer) {
	let formData = {
		groupId,
		userId,
		offer
	}
	return http({
		url: '/webrtc/group/offer',
		method: 'post',
		data: formData
	})
}

RtcGroupApi.prototype.answer = function (groupId, userId, answer) {
	let formData = {
		groupId,
		userId,
		answer
	}
	return http({
		url: '/webrtc/group/answer',
		method: 'post',
		data: formData
	})
}

RtcGroupApi.prototype.quit = function (groupId) {
	return http({
		url: '/webrtc/group/quit?groupId=' + groupId,
		method: 'post'
	})
}

RtcGroupApi.prototype.cancel = function (groupId) {
	return http({
		url: '/webrtc/group/cancel?groupId=' + groupId,
		method: 'post'
	})
}

RtcGroupApi.prototype.candidate = function (groupId, userId, candidate) {
	let formData = {
		groupId,
		userId,
		candidate
	}
	return http({
		url: '/webrtc/group/candidate',
		method: 'post',
		data: formData
	})
}

RtcGroupApi.prototype.device = function (groupId, isCamera, isMicroPhone) {
	let formData = {
		groupId,
		isCamera,
		isMicroPhone
	}
	return http({
		url: '/webrtc/group/device',
		method: 'post',
		data: formData
	})
}


RtcGroupApi.prototype.candidate = function (groupId, userId, candidate) {
	let formData = {
		groupId,
		userId,
		candidate
	}
	return http({
		url: '/webrtc/group/candidate',
		method: 'post',
		data: formData
	})
}

RtcGroupApi.prototype.heartbeat = function (groupId) {
	return http({
		url: '/webrtc/group/heartbeat?groupId=' + groupId,
		method: 'post'
	})
}

export default RtcGroupApi;