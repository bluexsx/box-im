

const request =  (options) => {
	const header = options.header||{};
	const loginInfo = uni.getStorageSync("loginInfo");
	if (loginInfo) {
		header.accessToken = loginInfo.accessToken;
	}
	return new Promise(function(resolve, reject) {
		uni.request({
			url:  process.env.BASE_URL + options.url,
			method: options.method || 'GET',
			header: header,
			data: options.data || {},
			async success(res) {
				if (res.data.code == 200) {
					return resolve(res.data.data)
				} else if (res.data.code == 400) {
					uni.navigateTo({
						url: '/pages/login/login'
					});
				} else if (res.data.code == 401) {
					console.log("token失效，尝试重新获取")
					if (!loginInfo) {
						uni.navigateTo({
							url: '/pages/login/login'
						});
					}
					// 发送请求, 进行刷新token操作, 获取新的token
					const data = await request({
						method: 'PUT',
						url: '/refreshToken',
						header: {
							refreshToken: loginInfo.refreshToken
						}
					})
					// 换取token失败，跳转至登录界面
					if(data.code != 200){
						uni.navigateTo({
							url: '/pages/login/login'
						});
					}
					// 保存token
					uni.setStorageSync("loginInfo", data);
					// 重新发送刚才的请求
					return request(options)
				} else {
					uni.showToast({
						icon: "error",
						title: res.data.message,
						duration: 1500
					})
					return reject(res.data.data)
				}
			},
			fail(error) {
				return reject(error)
			}
		});
	});
}

export default request;