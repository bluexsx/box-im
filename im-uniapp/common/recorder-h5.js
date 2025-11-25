import UNI_APP from '@/.env.js';

let rc = null;
let duration = 0;
let chunks = [];
let stream = null;
let startTime = null; 
let checkIsEnable = () => {
	if (origin.indexOf('https') === -1 && origin.indexOf('localhost') === -1 &&
		origin.indexOf('127.0.0.1') === -1) {
		uni.showToast({
			title: '请在https环境中使用录音功能',
			icon: 'error'
		})
		return false;
	}
	if (!navigator.mediaDevices || !window.MediaRecorder) {
		uni.showToast({
			title: '当前浏览器不支持录音',
			icon: 'error'
		})
		return false;
	}
	return true;
}

let start = () => {
	return navigator.mediaDevices.getUserMedia({ audio: true }).then(audioStream => {
		startTime = new Date().getTime();
		chunks = [];
		stream = audioStream;
		rc = new MediaRecorder(stream)
		rc.start()
	})
}

let close = () => {
	stream.getTracks().forEach((track) => {
		track.stop()
	})
	rc.stop()
}


let upload = () => {
	return new Promise((resolve, reject) => {
		rc.ondataavailable = (e) => {
			chunks.push(e.data)
		}
		rc.onstop = () => {
			if(!chunks[0].size){
				chunks = [];
				return; 
			}
			duration = (new Date().getTime() - startTime) / 1000;
			const newbolb = new Blob(chunks, { 'type': 'audio/mpeg' });
			const name = new Date().getDate() + '.mp3';
			const file = new File([newbolb], name)
			uni.uploadFile({
				url: UNI_APP.BASE_URL + '/file/upload',
				header: {
					accessToken: uni.getStorageSync("loginInfo").accessToken
				},
				file: file,
				name: 'file',
				// 最新版本(4.85)的hbuilderx有bug,如果filePath为空会直接触发fail回调
				filePath: 'x', 
				success: (res) => {
					let r = JSON.parse(res.data);
					if (r.code != 200) {
						console.log(res)
						reject(r.message);
					} else {
						const data = {
							duration: parseInt(duration),
							url: r.data
						}
						resolve(data);
					}
				},
				fail: (e) => {
					reject(e);
				}
			})
		}
	})
}

export {
	checkIsEnable,
	start,
	close,
	upload
}