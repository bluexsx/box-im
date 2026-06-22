const emoTextList = ['呲牙', '吃瓜', '色色', '可怜', '飞吻', '大哭', '流泪', '好赞', '偷笑', '书呆', '笑哭',
	'头晕', '捂脸', '开心', '崇拜', '拜托', '害羞', '捂嘴', '思考', '星星眼',
	'坏笑', '白眼', '双赞', '叹气', '紧张', '焦虑', '暴怒', '疑惑', '得意', '怀疑',
	'犯困', '惊讶', '欢呼', '无语', '裂开', '偷看', '挠头', '眨眼', '奋斗', '悄悄话', '好的',
	'点赞', '鼓掌', '挥手', '加油', '耶', '比心', 'OK', '指向', '摇滚', '差评'
];

const EMOJI_REGEX = /\[(?:OK|[\u4E00-\u9FA5]{1,3})\]/gi;

const formatEmoji = (word) => `[${word}]`;

const parseEmojiWord = (emoText) => {
	const match = String(emoText).match(/^\[(OK|[\u4E00-\u9FA5]{1,3})\]$/i);
	return match ? (match[1].toUpperCase() === 'OK' ? 'OK' : match[1]) : emoText;
};

const emojiContext = require.context('@/assets/emoji', false, /\.png$/);

const getEmojiUrl = (idx) => emojiContext(`./${idx}.png`);

let transform = (content, extClass) => {
	return content.replace(EMOJI_REGEX, (text) => textToImg(text, extClass));
}

// 将匹配结果替换表情图片
let textToImg = (emoText, extClass) => {
	let word = parseEmojiWord(emoText);
	let idx = emoTextList.indexOf(word);
	if (idx == -1) {
		return emoText;
	}
	let url = getEmojiUrl(idx);
	return `<img src="${url}" class="${extClass}" />`
}

let textToUrl = (emoText) => {
	let word = parseEmojiWord(emoText);
	let idx = emoTextList.indexOf(word);
	if (idx == -1) {
		return "";
	}
	return getEmojiUrl(idx);
}

const filterRecentEmojis = (list) => {
	return (list || []).filter(text => emoTextList.includes(text));
}

export default {
	emoTextList,
	EMOJI_REGEX,
	formatEmoji,
	parseEmojiWord,
	filterRecentEmojis,
	transform,
	textToImg,
	textToUrl
}
