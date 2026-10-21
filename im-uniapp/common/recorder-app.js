import { uploadRequest } from '@/common/request';

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
			uploadRequest({
				url: '/file/upload',
				filePath: wavFile.tempFilePath,
				name: 'file'
			}).then((r) => {
				resolve({
					duration: Math.round(duration),
					url: r.data
				});
			}).catch((e) => {
				reject(e && e.message ? e.message : e);
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