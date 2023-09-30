const BASE_URL = "http://192.168.43.6:8888"
const request =  (options) => {
	
	const header = options.header||{};
	const accessToken = uni.getStorageSync("accessToken");
	if (accessToken) {
		header.accessToken = accessToken;
	}
	return new Promise(function(resolve, reject) {
		uni.request({
			url: BASE_URL + options.url,
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
					const refreshToken = uni.getStorageSync("refreshToken");
					if (!refreshToken) {
						uni.navigateTo({
							url: '/pages/login/login'
						});
					}
					// 发送请求, 进行刷新token操作, 获取新的token
					const data = await request({
						method: 'PUT',
						url: '/refreshToken',
						header: {
							refreshToken: refreshToken
						}
					})
					// 换取token失败，跳转至登录界面
					if(data.code != 200){
						uni.navigateTo({
							url: '/pages/login/login'
						});
					}
					// 保存token
					uni.setStorageSync("accessToken", data.accessToken);
					uni.setStorageSync("refreshToken", data.refreshToken);
					// 这里需要把headers清掉，否则请求时会报错，原因暂不详...
					//response.config.headers=undefined;
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
				switch (error.response.status) {
					case 400:
						uni.showToast({
							title: error.response.data,
							type: 'error',
							duration: 1500,

						})
						break
					case 401:
						uni.navigateTo({
							url: '/pages/login/login'
						});
						break
					case 405:
						uni.showToast({
							title: 'http请求方式有误',
							icon: 'error',
							duration: 1500
						})
						break
					case 404:
					case 500:
						uni.showToast({
							title: '服务器出了点小差，请稍后再试',
							icon: 'error',
							duration: 1500
						})
						break
					case 501:
						uni.showToast({
							title: '服务器不支持当前请求所需要的某个功能',
							icon: 'error',
							duration: 1500
						})
						break
				}
				
				return reject(error)
			}
		});
	});
}

export default request;