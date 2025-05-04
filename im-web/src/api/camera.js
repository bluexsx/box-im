
class ImCamera {
	constructor() {
		this.stream = null;
	}
}

ImCamera.prototype.isEnable = function () {
	return !!navigator && !!navigator.mediaDevices && !!navigator.mediaDevices.getUserMedia;
}

ImCamera.prototype.openVideo = function () {
	return new Promise((resolve, reject) => {
		if (this.stream) {
			this.close()
		}
		let constraints = {
			video: true,
			audio: {
				echoCancellation: true, //音频开启回音消除
				noiseSuppression: true // 开启降噪
			}
		}
		navigator.mediaDevices.getUserMedia(constraints).then((stream) => {
			console.log("摄像头打开")
			this.stream = stream;
			resolve(stream);
		}).catch((e) => {
			console.log(e)
			console.log("摄像头未能正常打开")
			reject({
				code: 0,
				message: "摄像头未能正常打开"
			})
		})
	})
}


ImCamera.prototype.openAudio = function () {
	return new Promise((resolve, reject) => {
		let constraints = {
			video: false,
			audio: {
				echoCancellation: true, //音频开启回音消除
				noiseSuppression: true // 开启降噪
			}
		}
		navigator.mediaDevices.getUserMedia(constraints).then((stream) => {
			this.stream = stream;
			resolve(stream);
		}).catch(() => {
			console.log("麦克风未能正常打开")
			reject({
				code: 0,
				message: "麦克风未能正常打开"
			})
		})

	})
}

ImCamera.prototype.close = function () {
	// 停止流
	if (this.stream) {
		this.stream.getTracks().forEach((track) => {
			track.stop();
		});
	}
}

export default ImCamera;