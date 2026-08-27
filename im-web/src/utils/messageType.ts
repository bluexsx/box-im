/** 消息类型判断 */

// 是否普通消息
export const isNormal = (type: number) => {
  return type >= 0 && type < 10;
};

// 是否提示消息
export const isTip = (type: number) => {
  return type >= 20 && type < 30;
};

// 操作交互类消息
export const isAction = (type: number) => {
  return type >= 40 && type < 50;
};

// 单人通话信令
export const isRtcPrivate = (type: number) => {
  return type >= 100 && type < 200;
};

// 多人通话信令
export const isRtcGroup = (type: number) => {
  return type >= 200 && type < 300;
};
