import axios from 'axios'
import router from '@/router'
import qs from 'qs'
import merge from 'lodash/merge'
import {
	Message
} from 'element-ui'

const http = axios.create({
	timeout: 1000 * 30,
	withCredentials: true,
	headers: {
		'Content-Type': 'application/json; charset=utf-8'
	}
})

/**
 * 请求拦截
 */
http.interceptors.request.use(config => {
	// todo 请求头带上token
	return config
}, error => {
	return Promise.reject(error)
})

/**
 * 响应拦截
 */
http.interceptors.response.use(response => {
	if (response.data.code == 200) {
		return response.data.data;
	} else {
		Message({
			message: response.data.message,
			type: 'error',
			duration: 1500,
			customClass: 'element-error-message-zindex'
		})
		
		if (response.data.code == 401) {
			router.replace("/login");
		}
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
			router.replace("/login");
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
