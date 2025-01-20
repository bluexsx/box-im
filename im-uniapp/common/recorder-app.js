import UNI_APP from '@/.env.js';

const rc = uni.getRecorderManager();
// 录音开始时间
let startTime = null;
let checkIsEnable = ()=>{
	return true;
}

let start = () => {
	return new Promise((resolve, reject) => {
		rc.onStart(() => {
			startTime = new Date();
			resolve()
		});
		rc.onError((e) => {
			console.log(e);
			reject(e)
		})
		rc.start({
			format: 'mp3' // 录音格式，可选值：aac/mp3
		});
	})
}

let close = () => {
	rc.stop();
}

let upload = () => {
	return new Promise((resolve, reject) => {
		rc.onStop((wavFile, a, b) => {
			uni.uploadFile({
				url: UNI_APP.BASE_URL + '/file/upload',
				header: {
					accessToken: uni.getStorageSync("loginInfo").accessToken
				},
				filePath: wavFile.tempFilePath,
				name: 'file',
				success: (res) => {
					let r = JSON.parse(res.data);
					if (r.code != 200) {
						reject(r.message);
					} else {
						const duration = (new Date().getTime() - startTime.getTime()) /
							1000
						const data = {
							duration: Math.round(duration),
							url: r.data
						}
						resolve(data);
					}
				},
				fail: (e) => {
					reject(e);
				}
			})
		});
	})
}

export {
	checkIsEnable,
	start,
	close,
	upload
}