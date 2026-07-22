import UNI_APP from '@/.env.js';

const rc = uni.getRecorderManager();
// 录音开始时间
let startTime = null;
// 录音时长
let duration = 0;
let checkIsEnable = ()=>{
	return true;
}

let start = () => {
	return new Promise((resolve, reject) => {
		rc.onStart(() => {
			startTime = new Date();
			duration = 0;
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
	duration = (new Date().getTime() - startTime) / 1000;
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