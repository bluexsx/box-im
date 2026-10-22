import UNI_APP from '@/.env.js'

let requestList = [];
let isRefreshToken = false;

const toast = (title) => uni.showToast({ title, icon: 'none', duration: 1500 });

const buildHeader = () => {
	const header = {};
	const loginInfo = uni.getStorageSync("loginInfo");
	if (loginInfo) {
		header.accessToken = loginInfo.accessToken;
	}
	return header;
};

const reqRefreshToken = (loginInfo) => {
	return new Promise((resolve, reject) => {
		uni.request({
			method: 'PUT',
			url: UNI_APP.BASE_URL + '/refreshToken',
			header: {
				refreshToken: loginInfo.refreshToken
			},
			success: resolve,
			fail: reject
		});
	});
};

/** 刷新 token；并发排队。成功 true，失败 exit 后 false */
const refreshAccessToken = async (loginInfo) => {
	if (isRefreshToken) {
		return new Promise((resolve) => requestList.push(resolve));
	}
	isRefreshToken = true;
	console.log("token失效，尝试重新获取");
	const failRefresh = () => {
		const queue = requestList;
		requestList = [];
		isRefreshToken = false;
		queue.forEach(cb => cb(false));
		console.log("刷新token失败");
		getApp().$vm.exit();
		return false;
	};
	try {
		const res = await reqRefreshToken(loginInfo);
		if (!res || res.data.code != 200) {
			return failRefresh();
		}
		uni.setStorageSync("loginInfo", res.data.data);
		const queue = requestList;
		requestList = [];
		isRefreshToken = false;
		queue.forEach(cb => cb(true));
		return true;
	} catch (e) {
		return failRefresh();
	}
};

const request = (options) => {
	const header = Object.assign(buildHeader(), options.header || {});
	const loginInfo = uni.getStorageSync("loginInfo");
	return new Promise((resolve, reject) => {
		uni.request({
			url: UNI_APP.BASE_URL + options.url,
			method: options.method || 'GET',
			header,
			data: options.data || {},
			timeout: options.timeout || 30000,
			async success(res) {
				if (res.data.code == 200) {
					return resolve(res.data.data);
				}
				if (res.data.code == 400) {
					getApp().$vm.exit();
					return;
				}
				if (res.data.code == 401) {
					if (!await refreshAccessToken(uni.getStorageSync("loginInfo") || loginInfo)) {
						return;
					}
					return resolve(request(options));
				}
				toast(res.data.message);
				return reject(res.data);
			},
			fail(error) {
				toast("网络似乎有点不给力哟");
				return reject(error);
			}
		});
	});
};

/** 上传；401 时与 request 共用刷新队列并重试。成功返回完整 Result */
const uploadRequest = (options) => {
	return new Promise((resolve, reject) => {
		const opts = {
			url: UNI_APP.BASE_URL + options.url,
			name: options.name || 'file',
			header: buildHeader(),
			timeout: options.timeout || 60000,
			success: async (res) => {
				let body;
				try {
					body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
				} catch (e) {
					toast("网络似乎有点不给力哟");
					return reject(e);
				}
				if (body.code == 200) {
					return resolve(body);
				}
				if (body.code == 400) {
					getApp().$vm.exit();
					return;
				}
				if (body.code == 401) {
					if (!await refreshAccessToken(uni.getStorageSync("loginInfo"))) {
						return;
					}
					try {
						return resolve(await uploadRequest(options));
					} catch (e) {
						return reject(e);
					}
				}
				toast(body.message);
				return reject(body);
			},
			fail: (error) => {
				toast("网络似乎有点不给力哟");
				return reject(error);
			}
		};
		if (options.filePath != null) opts.filePath = options.filePath;
		if (options.file != null) opts.file = options.file;
		if (options.formData != null) opts.formData = options.formData;
		uni.uploadFile(opts);
	});
};

export { uploadRequest };
export default request;
