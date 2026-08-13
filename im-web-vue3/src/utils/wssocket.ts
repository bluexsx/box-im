type WsCallback = (...args: unknown[]) => void;
type MessageCallback = (cmd: number, data: unknown) => void;

let websock: WebSocket | null = null;
// 断线重连后，延迟5秒重新创建WebSocket连接  rec用来存储延迟请求的代码
let rec: ReturnType<typeof setTimeout> | null = null;
// 连接标识 避免重复连接
let isConnect = false;
let connectCallBack: WsCallback | null = null;
let messageCallBack: MessageCallback | null = null;
let closeCallBack: WsCallback | null = null;
const devId = Math.floor(Math.random() * 1000000);

// 心跳设置
const heartCheck = {
  timeout: 20000, // 每段时间发送一次心跳包 这里设置为20s
  timeoutObj: null as ReturnType<typeof setTimeout> | null, // 延时发送消息对象（启动心跳新建这个对象，收到消息后重置对象）
  start: () => {
    if (isConnect && websock) {
      websock.send(JSON.stringify({ cmd: 1, data: {} }));
    }
  },
  reset: () => {
    if (heartCheck.timeoutObj) {
      clearTimeout(heartCheck.timeoutObj);
    }
    heartCheck.timeoutObj = setTimeout(() => {
      heartCheck.start();
    }, heartCheck.timeout);
  }
};

export const connect = (wsurl: string, accessToken: string) => {
  try {
    if (isConnect) {
      return;
    }
    websock = new WebSocket(wsurl);
    websock.onmessage = (e) => {
      const sendInfo = JSON.parse(e.data as string) as { cmd: number; data: unknown };
      if (sendInfo.cmd === 0) {
        heartCheck.start();
        // 登录成功才算真正完成连接
        connectCallBack?.();
      } else if (sendInfo.cmd === 1) {
        // 重新开启心跳定时
        heartCheck.reset();
      } else {
        // 其他消息转发出去
        messageCallBack?.(sendInfo.cmd, sendInfo.data);
      }
    };
    websock.onclose = (e) => {
      isConnect = false; // 断开后修改标识
      closeCallBack?.(e);
    };
    websock.onopen = () => {
      isConnect = true;
      // 发送登录命令
      websock?.send(
        JSON.stringify({
          cmd: 0,
          data: {
            accessToken,
            devId
          }
        })
      );
    };
    // 连接发生错误的回调方法
    websock.onerror = (e) => {
      close(3000);
      isConnect = false;
      closeCallBack?.(e);
    };
  } catch {
    // 如果无法连接上webSocket 那么重新连接！可能会因为服务器重新部署，或者短暂断网等导致无法创建连接
    reconnect(wsurl, accessToken);
  }
};

// 定义重连函数
export const reconnect = (wsurl: string, accessToken: string) => {
  if (isConnect) {
    // 如果已经连上就不在重连了
    return;
  }
  if (rec) {
    clearTimeout(rec);
  }
  // 延迟5秒重连  避免过多次过频繁请求重连
  rec = setTimeout(() => {
    connect(wsurl, accessToken);
  }, 15000);
};

// 设置关闭连接
export const close = (code?: number) => {
  if (!isConnect) {
    return;
  }
  websock?.close(code);
};

// 实际调用的方法
export const sendMessage = (agentData: unknown) => {
  if (!websock) {
    // 若未开启 ，则等待1s后重新调用
    setTimeout(() => sendMessage(agentData), 1000);
    return;
  }
  if (websock.readyState === WebSocket.OPEN) {
    // 若是ws开启状态
    websock.send(JSON.stringify(agentData));
    return;
  }
  // 若是 正在开启状态，则等待1s后重新调用
  setTimeout(() => sendMessage(agentData), 1000);
};

export const onConnect = (callback: WsCallback) => {
  connectCallBack = callback;
};

export const onMessage = (callback: MessageCallback) => {
  messageCallBack = callback;
};

export const onClose = (callback: WsCallback) => {
  closeCallBack = callback;
};
