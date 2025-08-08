import UNI_APP from '@/.env.js'

// 请求队列
let requestList = [];
// 是否正在刷新中
let isRefreshToken = false;

const request = (options) => {
	const header = options.header || {};
	const loginInfo = uni.getStorageSync("loginInfo");
	if (loginInfo) {
		header.accessToken = loginInfo.accessToken;
	}
	return new Promise(function (resolve, reject) {
		uni.request({
			url: UNI_APP.BASE_URL + options.url,
			method: options.method || 'GET',
			header: header,
			data: options.data || {},
			timeout: options.timeout || 3000,
			async success(res) {
				if (res.data.code == 200) {
					return resolve(res.data.data)
				} else if (res.data.code == 400) {
					getApp().$vm.exit();
				} else if (res.data.code == 401) {
					console.log("token失效，尝试重新获取")
					if (isRefreshToken) {
						// 正在刷新token,把其他请求存起来
						requestList.push(() => {
							resolve(request(options))
						})
						return;
					}
					isRefreshToken = true;
					// 发送请求, 进行刷新token操作, 获取新的token
					const res = await reqRefreshToken(loginInfo);
					if (!res || res.data.code != 200) {
						requestList = [];
						isRefreshToken = false;
						console.log("刷新token失败")
						getApp().$vm.exit();
						return;
					}
					let newInfo = res.data.data;
					uni.setStorageSync("loginInfo", newInfo);
					requestList.forEach(cb => cb());
					requestList = [];
					isRefreshToken = false;
					// 重新发送刚才的请求
					return resolve(request(options))

				} else {
					uni.showToast({
						icon: "none",
						title: res.data.message,
						duration: 1500
					})
					return reject(res.data)
				}
			},
			fail(error) {
				uni.showToast({
					title: "网络似乎有点不给力哟",
					icon: "none",
					duration: 1500
				})
				return reject(error)
			}
		});
	});
}


const reqRefreshToken = (loginInfo) => {
	return new Promise(function (resolve, reject) {
		uni.request({
			method: 'PUT',
			url: UNI_APP.BASE_URL + '/refreshToken',
			header: {
				refreshToken: loginInfo.refreshToken
			},
			success: (res) => {
				resolve(res);
			},
			fail: (res) => {
				reject(res);
			}
		});
	});
}

export default request;