import UNI_APP from '@/.env.js'

const emoTextList = ['憨笑', '媚眼', '开心', '坏笑', '可怜', '爱心', '笑哭', '拍手', '惊喜', '打气',
	'大哭', '流泪', '饥饿', '难受', '健身', '示爱', '色色', '眨眼', '暴怒', '惊恐',
	'思考', '头晕', '大吐', '酷笑', '翻滚', '享受', '鼻涕', '快乐', '雀跃', '微笑',
	'贪婪', '红心', '粉心', '星星', '大火', '眼睛', '音符', "叹号", "问号", "绿叶",
	"燃烧", "喇叭", "警告", "信封", "房子", "礼物", "点赞", "举手", "拍手", "点头",
	"摇头", "偷瞄", "庆祝", "疾跑", "打滚", "惊吓", "起跳"
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
	return UNI_APP.EMO_URL + idx + ".gif";
}

export default {
	containEmoji,
	emoTextList,
	EMOJI_REGEX,
	formatEmoji,
	parseEmojiWord,
	transform,
	textToPath
}
