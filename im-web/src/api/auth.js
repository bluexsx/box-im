import http from './httpRequest.js'

const AUTO_LOGIN_KEY = 'isAutoLogin';
const USERNAME_KEY = 'username';
const REFRESH_TOKEN_KEY = 'refreshToken';


export const getSavedUsername = () => {
	return localStorage.getItem(USERNAME_KEY) || '';
};

export const isAutoLoginEnabled = () => {
	const value = localStorage.getItem(AUTO_LOGIN_KEY);
	if (value == null) {
		return true;
	}
	return JSON.parse(value);
};

export const getPersistedRefreshToken = () => {
	return localStorage.getItem(REFRESH_TOKEN_KEY) || '';
};

export const isLoggedIn = () => {
	return !!sessionStorage.getItem('accessToken');
};

export const getRefreshToken = () => {
	return sessionStorage.getItem('refreshToken') || getPersistedRefreshToken();
};

/**
 * 登录成功后保存会话
 * @param {{ accessToken: string, refreshToken: string }} data
 * @param {{ autoLogin: boolean, userName?: string }} options
 */
export const saveLoginSession = (data, { autoLogin, userName }) => {
	sessionStorage.setItem('accessToken', data.accessToken);
	sessionStorage.setItem('refreshToken', data.refreshToken);
	localStorage.setItem(AUTO_LOGIN_KEY, JSON.stringify(autoLogin));
	if (userName) {
		localStorage.setItem(USERNAME_KEY, userName);
	}
	if (autoLogin) {
		localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
	} else {
		localStorage.removeItem(REFRESH_TOKEN_KEY);
	}
};

/** 刷新 token 后同步更新存储 */
export const saveTokens = (data) => {
	sessionStorage.setItem('accessToken', data.accessToken);
	sessionStorage.setItem('refreshToken', data.refreshToken);
	if (isAutoLoginEnabled()) {
		localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
	}
};

/**
 * 退出登录
 * @param {boolean} clearAutoLogin 是否清除自动登录
 */
export const clearLoginSession = (clearAutoLogin = true) => {
	sessionStorage.removeItem('accessToken');
	sessionStorage.removeItem('refreshToken');
	if (clearAutoLogin) {
		localStorage.setItem(AUTO_LOGIN_KEY, 'false');
		localStorage.removeItem(REFRESH_TOKEN_KEY);
	}
};

/**
 * 使用本地 refreshToken 自动登录
 */
export const refreshLogin = () => {
	const refreshToken = getPersistedRefreshToken();
	if (!refreshToken) {
		return Promise.reject(new Error('no refresh token'));
	}
	return http({
		method: 'put',
		url: '/refreshToken',
		headers: {
			refreshToken: refreshToken
		}
	}).then((data) => {
		saveTokens(data);
		return data;
	});
};
