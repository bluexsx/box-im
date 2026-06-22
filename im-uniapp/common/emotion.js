import UNI_APP from '@/.env.js'

const emoTextList = ['呲牙', '吃瓜', '色色', '可怜', '飞吻', '大哭', '流泪', '好赞', '偷笑', '书呆', '笑哭',
	'头晕', '捂脸', '开心', '崇拜', '拜托', '害羞', '捂嘴', '思考', '星星眼',
	'坏笑', '白眼', '双赞', '叹气', '紧张', '焦虑', '暴怒', '疑惑', '得意', '怀疑',
	'犯困', '惊讶', '欢呼', '无语', '裂开', '偷看', '挠头', '眨眼', '奋斗', '悄悄话', '好的',
	'点赞', '鼓掌', '挥手', '加油', '耶', '比心', 'OK', '指向', '摇滚', '差评'
];

const EMOJI_REGEX = /\[[\u4E00-\u9FA5]{1,3}\]/gi;

const formatEmoji = (word) => `[${word}]`;

const parseEmojiWord = (emoText) => {
	const match = String(emoText).match(/^\[([\u4E00-\u9FA5]{1,3})\]$/);
	return match ? match[1] : emoText;
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
