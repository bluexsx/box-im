type IceCallback = (candidate: RTCIceCandidate) => void;
type StateCallback = (state: string) => void;
type TrackCallback = (stream: MediaStream) => void;

class ImWebRtc {
  configuration: RTCConfiguration = {};
  stream: MediaStream | null = null;
  peerConnection: RTCPeerConnection | null = null;
  videoSender: RTCRtpSender | null = null;
  audioSender: RTCRtpSender | null = null;

  isEnable = () => {
    const w = window as Window & {
      RTCPeerConnection?: typeof RTCPeerConnection;
      webkitRTCPeerConnection?: typeof RTCPeerConnection;
      mozRTCPeerConnection?: typeof RTCPeerConnection;
      RTCSessionDescription?: typeof RTCSessionDescription;
      webkitRTCSessionDescription?: typeof RTCSessionDescription;
      mozRTCSessionDescription?: typeof RTCSessionDescription;
      RTCIceCandidate?: typeof RTCIceCandidate;
      webkitRTCIceCandidate?: typeof RTCIceCandidate;
      mozRTCIceCandidate?: typeof RTCIceCandidate;
    };
    w.RTCPeerConnection = w.RTCPeerConnection || w.webkitRTCPeerConnection || w.mozRTCPeerConnection;
    w.RTCSessionDescription = w.RTCSessionDescription || w.webkitRTCSessionDescription || w.mozRTCSessionDescription;
    w.RTCIceCandidate = w.RTCIceCandidate || w.webkitRTCIceCandidate || w.mozRTCIceCandidate;
    return !!w.RTCPeerConnection;
  };

  init = (configuration: RTCConfiguration) => {
    this.configuration = configuration;
  };

  setupPeerConnection = (callback: TrackCallback) => {
    this.peerConnection = new RTCPeerConnection(this.configuration);
    this.peerConnection.ontrack = (e) => {
      // 对方的视频流
      callback(e.streams[0]);
    };
  };

  setStream = (stream: MediaStream | null) => {
    if (this.peerConnection) {
      this.peerConnection.getSenders().forEach((sender) => {
        this.peerConnection!.removeTrack(sender);
      });
    }
    this.videoSender = null;
    this.audioSender = null;
    if (stream && this.peerConnection) {
      stream.getTracks().forEach((track) => {
        const sender = this.peerConnection!.addTrack(track, stream);
        if (track.kind === 'video') {
          this.videoSender = sender;
        } else if (track.kind === 'audio') {
          this.audioSender = sender;
        }
      });
    }
    this.stream = stream;
  };

  // 替换当前发送的音视频轨道（关摄像头后 sender.track 为空，须用固定 sender 才能再次 replaceTrack）
  switchStream = (stream: MediaStream) => {
    const videoTrack = stream.getVideoTracks()[0];
    const audioTrack = stream.getAudioTracks()[0];
    if (this.videoSender) {
      void this.videoSender.replaceTrack(videoTrack);
    }
    if (this.audioSender) {
      void this.audioSender.replaceTrack(audioTrack);
    }
    this.stream = stream;
  };

  onIcecandidate = (callback: IceCallback) => {
    if (!this.peerConnection) return;
    this.peerConnection.onicecandidate = (event) => {
      // 追踪到候选信息
      if (event.candidate) {
        callback(event.candidate);
      }
    };
  };

  onStateChange = (callback: StateCallback) => {
    if (!this.peerConnection) return;
    // 监听连接状态
    this.peerConnection.oniceconnectionstatechange = (event) => {
      const state = (event.target as RTCPeerConnection).iceConnectionState;
      callback(state);
    };
  };

  createOffer = () => {
    return new Promise<RTCSessionDescriptionInit>((resolve, reject) => {
      if (!this.peerConnection) {
        reject(new Error('peerConnection not ready'));
        return;
      }
      // 创建本地sdp信息
      this.peerConnection
        .createOffer({ offerToReceiveAudio: true, offerToReceiveVideo: true })
        .then((offer) => {
          // 设置本地sdp信息
          void this.peerConnection!.setLocalDescription(offer);
          // 发起呼叫请求
          resolve(offer);
        })
        .catch((e) => reject(e));
    });
  };

  createAnswer = (offer: RTCSessionDescriptionInit) => {
    return new Promise<RTCSessionDescriptionInit>((resolve, reject) => {
      if (!this.peerConnection) {
        reject(new Error('peerConnection not ready'));
        return;
      }
      // 设置远端的sdp
      this.setRemoteDescription(offer);
      // 创建本地dsp
      this.peerConnection
        .createAnswer({ offerToReceiveAudio: true, offerToReceiveVideo: true })
        .then((answer) => {
          // 设置本地sdp信息
          void this.peerConnection!.setLocalDescription(answer);
          // 接受呼叫请求
          resolve(answer);
        })
        .catch((e) => reject(e));
    });
  };

  setRemoteDescription = (offer: RTCSessionDescriptionInit) => {
    if (!this.peerConnection) return;
    // 设置对方的sdp信息
    void this.peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
  };

  addIceCandidate = (candidate: RTCIceCandidateInit) => {
    if (!this.peerConnection) return;
    // 添加对方的候选人信息
    void this.peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
  };

  close = () => {
    // 关闭RTC连接
    if (this.peerConnection) {
      this.peerConnection.close();
      this.peerConnection.onicecandidate = null;
      this.peerConnection = null;
    }
    this.videoSender = null;
    this.audioSender = null;
    this.stream = null;
  };
}

export default ImWebRtc;
