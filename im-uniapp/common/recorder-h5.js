import UNI_APP from '@/.env.js';

let rc = null;
let duration = 0;
let chunks = [];
let stream = null;
let start = () => {
	return navigator.mediaDevices.getUserMedia({ audio: true }).then(audioStream => {
		const startTime = new Date().getTime();
		chunks = [];
		stream = audioStream;
		rc = new MediaRecorder(stream)
		rc.ondataavailable = (e) => {
			console.log("ondataavailable")
			chunks.push(e.data)
		}
		rc.onstop = () => {
			duration = (new Date().getTime() - startTime) / 1000;
			console.log("时长：", duration)
		}
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
		setTimeout(() => {
			const newbolb = new Blob(chunks, { 'type': 'audio/mpeg' });
			const name = new Date().getDate() + '.mp3';
			const file = new File([newbolb], name)
			console.log("upload")
			uni.uploadFile({
				url: UNI_APP.BASE_URL + '/file/upload',
				header: {
					accessToken: uni.getStorageSync("loginInfo").accessToken
				},
				file: file,
				name: 'file',
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
		}, 100)
	})
}

export {
	start,
	close,
	upload
}