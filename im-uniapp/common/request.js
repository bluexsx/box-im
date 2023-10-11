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
	return new Promise(function(resolve, reject) {
		uni.request({
			url: process.env.BASE_URL + options.url,
			method: options.method || 'GET',
			header: header,
			data: options.data || {},
			async success(res) {
				if (res.data.code == 200) {
					return resolve(res.data.data)
				} else if (res.data.code == 400) {
					navToLogin();
				} else if (res.data.code == 401) {
					console.log("token失效，尝试重新获取")
					if (isRefreshToken) {
						// 正在刷新token,把其他请求存起来
						return new Promise(resolve => {
							requestList.push(() => {
								resolve(request(options))
							})
						})
					}
					isRefreshToken = true;
					// 发送请求, 进行刷新token操作, 获取新的token
					const res = await reqRefreshToken(loginInfo).catch((res) => {
						return navToLogin();
					}).finally(()=>{
						requestList.forEach(cb => cb());
						requestList = [];
						isRefreshToken = false;
					})
					if (res.data.code != 200) {
						return navToLogin();
					}
					// 保存token
					uni.setStorageSync("loginInfo", res.data.data);
					// 重新发送刚才的请求
					return request(options)

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
					title: "网络似乎有点不给力，请稍后重试",
					duration: 1500
				})
				return reject(error)
			}
		});
	});
}


const reqRefreshToken = (loginInfo) => {
	return new Promise(function(resolve, reject) {
		uni.request({
			method: 'PUT',
			url: process.env.BASE_URL + '/refreshToken',
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


const navToLogin = () => {
	uni.showToast({
		icon: "none",
		title: "登录过期，请需要重新登录",
		duration: 1500
	})
	uni.removeStorageSync("loginInfo");
	uni.navigateTo({
		url: '/pages/login/login'
	});
}
export default request;