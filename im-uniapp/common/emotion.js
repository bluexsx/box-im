import UNI_APP from '@/.env.js'

const emoTextList = [
	'六六六', '微笑', '抱心', '捂脸', '点赞', '笑哭', '读书', '听歌', '期待', '飞吻',
	'吃瓜', '可怜', '惊讶', '生气', '困', '思考', '拜托', '怒火', '笑指', '疑惑',
	'书呆', '晕', '星眼', '伤心', '无语', '好的', '比耶', '呲牙', '奶茶', '放大',
	'大哭', '庆祝', '花痴', '闭嘴', '吐舌', '鼻涕', '爆头', '喊话', '吐彩', '嘘',
	'酷笑', '斜眼', '憨笑', '担心', '心动', '害羞', '祈祷', '土豪', '发财', '叹气',
	'紧张', '口罩', '鼓掌', '挥手', '耶', '比心', 'OK', '指上', '指右', '摇滚',
	'合十', '碰拳', '握拳', '赞', '倒赞', '六', '大便', '蛋糕', '红包', '礼花',
	'咖啡', '西瓜', '月亮'
];

const EMOJI_REGEX = /\[(?:OK|[\u4E00-\u9FA5]{1,3})\]/gi;

const formatEmoji = (word) => `[${word}]`;

const parseEmojiWord = (emoText) => {
	const match = String(emoText).match(/^\[(OK|[\u4E00-\u9FA5]{1,3})\]$/i);
	return match ? (match[1].toUpperCase() === 'OK' ? 'OK' : match[1]) : emoText;
};

let containEmoji = (content) => {
	return EMOJI_REGEX.test(content)
}

let transform = (content, extClass) => {
	return content.replace(EMOJI_REGEX, (emoText) => {
		let word = parseEmojiWord(emoText);
		let idx = emoTextList.indexOf(word);
		if (idx == -1) {
			return emoText;
		}
		let path = textToPath(emoText);
		let img = `<img src="${path}" class="${extClass}"/>`;
		return img;
	});
}

let textToPath = (emoText) => {
	let word = parseEmojiWord(emoText);
	let idx = emoTextList.indexOf(word);
	if (idx == -1) {
		return "";
	}
	return UNI_APP.EMO_URL + idx + ".png";
}

const filterRecentEmojis = (list) => {
	return (list || []).filter(text => emoTextList.includes(text));
}

export default {
	containEmoji,
	emoTextList,
	EMOJI_REGEX,
	formatEmoji,
	parseEmojiWord,
	filterRecentEmojis,
	transform,
	textToPath
}
