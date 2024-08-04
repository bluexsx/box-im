import axios from 'axios'

import { Message } from 'element-ui'

const http = axios.create({
	baseURL: process.env.VUE_APP_BASE_API,
	timeout: 1000 * 30,
	withCredentials: true
})

/**
 * 请求拦截
 */
http.interceptors.request.use(config => {
	let accessToken = sessionStorage.getItem("accessToken");
	if (accessToken) {
		config.headers.accessToken = encodeURIComponent(accessToken);
	}
	return config
}, error => {
	return Promise.reject(error)
})

/**
 * 响应拦截
 */
http.interceptors.response.use(async response => {
	if (response.data.code == 200) {
		return response.data.data;
	} else if (response.data.code == 400) {
		location.href = "/";
	} else if (response.data.code == 401) {
		console.log("token失效，尝试重新获取")
		let refreshToken = sessionStorage.getItem("refreshToken");
		if (!refreshToken) {
			location.href = "/";
		}
		// 发送请求, 进行刷新token操作, 获取新的token
		const data = await http({
			method: 'put',
			url: '/refreshToken',
			headers: {
				refreshToken: refreshToken
			}
		}).catch(()=>{
			location.href = "/";
		})
		// 保存token
		sessionStorage.setItem("accessToken", data.accessToken);
		sessionStorage.setItem("refreshToken", data.refreshToken);
		// 重新发送刚才的请求
		return http(response.config)
	} else {
		Message({
			message: response.data.message,
			type: 'error',
			duration: 1500,
			customClass: 'element-error-message-zindex'
		})
		return Promise.reject(response.data)
	}
}, error => {
	switch (error.response.status) {
		case 400:
			Message({
				message: error.response.data,
				type: 'error',
				duration: 1500,
				customClass: 'element-error-message-zindex'
			})
			break
		case 401:
			location.href = "/";
			break
		case 405:
			Message({
				message: 'http请求方式有误',
				type: 'error',
				duration: 1500,
				customClass: 'element-error-message-zindex'
			})
			break
		case 404:
		case 500:
			Message({
				message: '服务器出了点小差，请稍后再试',
				type: 'error',
				duration: 1500,
				customClass: 'element-error-message-zindex'
			})
			break
		case 501:
			Message({
				message: '服务器不支持当前请求所需要的某个功能',
				type: 'error',
				duration: 1500,
				customClass: 'element-error-message-zindex'
			})
			break
	}

	return Promise.reject(error)
})


export default http
