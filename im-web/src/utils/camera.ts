class ImCamera {
  stream: MediaStream | null = null;

  isEnable = () => {
    return !!navigator?.mediaDevices?.getUserMedia;
  };

  openVideo = () => {
    return new Promise<MediaStream>((resolve, reject) => {
      const constraints: MediaStreamConstraints = {
        video: true,
        audio: {
          echoCancellation: true, //音频开启回音消除
          noiseSuppression: true // 开启降噪
        }
      };
      navigator.mediaDevices
        .getUserMedia(constraints)
        .then((stream) => {
          this.stopStream();
          this.stream = stream;
          resolve(stream);
        })
        .catch((e) => {
          console.log(e);
          reject({ code: 0, message: '摄像头未能正常打开' });
        });
    });
  };

  openAudio = () => {
    return new Promise<MediaStream>((resolve, reject) => {
      const constraints: MediaStreamConstraints = {
        video: false,
        audio: {
          echoCancellation: true, //音频开启回音消除
          noiseSuppression: true // 开启降噪
        }
      };
      navigator.mediaDevices
        .getUserMedia(constraints)
        .then((stream) => {
          this.stopStream();
          this.stream = stream;
          resolve(stream);
        })
        .catch(() => {
          reject({ code: 0, message: '麦克风未能正常打开' });
        });
    });
  };

  openScreen = (withAudio?: boolean) => {
    return new Promise<MediaStream>((resolve, reject) => {
      navigator.mediaDevices
        .getDisplayMedia({ video: true })
        .then((screenStream) => {
          this.stopStream();
          // 默认投屏没有声音，这里补充音频流
          if (withAudio) {
            this.openAudio().then((audioStream) => {
              this.stream = new MediaStream();
              screenStream.getVideoTracks().forEach((track) => this.stream!.addTrack(track));
              audioStream.getAudioTracks().forEach((track) => this.stream!.addTrack(track));
              resolve(this.stream!);
            });
          } else {
            this.stream = screenStream;
            resolve(this.stream);
          }
        })
        .catch((e) => {
          console.log('获取屏幕画面失败:', e);
          reject({ code: 0, message: '获取屏幕画面失败' });
        });
    });
  };

  stopStream = () => {
    // 停止流
    if (this.stream) {
      this.stream.getTracks().forEach((track) => {
        track.stop();
      });
      this.stream = null;
    }
  };

  close = () => {
    this.stopStream();
  };
}

export default ImCamera;
