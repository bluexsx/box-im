import Recorder from 'js-audio-recorder';
import UNI_APP from '@/.env.js';

let rc = null;
let start = () => {
	if(rc != null){
		close();
	}
	rc = new Recorder();
	return rc.start();
}

let pause = () => {
	rc.pause();
}

let close = () => {
	rc.destroy();
	rc = null;
}

let upload = () => {
	return new Promise((resolve, reject) => {
		const wavBlob = rc.getWAVBlob();
		const newbolb = new Blob([wavBlob], { type: 'audio/wav'})
		const name = new Date().getDate() + '.wav';
		const file = new File([newbolb], name)
		uni.uploadFile({
			url: UNI_APP.BASE_URL + '/file/upload',
			header: {
				accessToken: uni.getStorageSync("loginInfo").accessToken
			},
			file: file,
			name: 'file',
			success: (res) => {
				let r = JSON.parse(res.data);
				if(r.code != 200){
					console.log(res)
					reject(r.message);
				}else {
					const data = {
						duration: parseInt(rc.duration),
						url: r.data
					}
					resolve(data);
				}
			},
			fail: (e) => {
				reject(e);
			}
		})
	})
}

export {
	start,
	pause,
	close,
	upload
}