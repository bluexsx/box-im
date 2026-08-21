import { MESSAGE_TYPE } from "./enums.js";
import useUserStore from '../store/userStore.js';

function previewContent(m) {
    let content = '[暂不支持该消息类型]';
    try {
        if (m.type == MESSAGE_TYPE.IMAGE) {
            content = '[图片]';
        } else if (m.type == MESSAGE_TYPE.VIDEO) {
            content = '[视频]';
        } else if (m.type == MESSAGE_TYPE.FILE) {
            content = '[文件] ' + JSON.parse(m.content).name;
        } else if (m.type == MESSAGE_TYPE.AUDIO) {
            content = '[语音] ' + JSON.parse(m.content).duration + '"';
        } else if (m.type == MESSAGE_TYPE.ACT_RT_VOICE) {
            content = '[语音通话]';
        } else if (m.type == MESSAGE_TYPE.ACT_RT_VIDEO) {
            content = '[视频通话]';
        } else if (m.type == MESSAGE_TYPE.SYSTEM_MESSAGE) {
            content = m.title;
        } else if (m.type == MESSAGE_TYPE.TEXT || m.type == MESSAGE_TYPE.RECALL) {
            content = m.content;
        } else if (m.type == MESSAGE_TYPE.TIP_TEXT) {
            content = previewTip(m.content);
        }
    } catch (e) {
        console.log("message:", m, e);
    }
    return content;
}

function previewTip(messageTip) {
    return messageTip || '';
}

export {
    previewContent,
    previewTip
};
