const emoTextList = ['憨笑', '媚眼', '开心', '坏笑', '可怜', '爱心', '笑哭', '拍手', '惊喜', '打气',
	'大哭', '流泪', '饥饿', '难受', '健身', '示爱', '色色', '眨眼', '暴怒', '惊恐',
	'思考', '头晕', '大吐', '酷笑', '翻滚', '享受', '鼻涕', '快乐', '雀跃', '微笑',
	'贪婪', '红心', '粉心', '星星', '大火', '眼睛', '音符', "叹号", "问号", "绿叶",
	"燃烧", "喇叭", "警告", "信封", "房子", "礼物", "点赞", "举手", "拍手", "点头",
	"摇头", "偷瞄", "庆祝", "疾跑", "打滚", "惊吓", "起跳"
];

const regex = /\#[\u4E00-\u9FA5]{1,3}\;/gi;

let containEmoji = (content) => {
	return regex.test(content)
}

let transform = (content, extClass) => {
	return content.replace(regex, (emoText) => {
		// 将匹配结果替换表情图片
		let word = emoText.replace(/\#|\;/gi, '');
		let idx = emoTextList.indexOf(word);
		if (idx == -1) {
			return emoText;
		}
		let path = textToPath(emoText, true);
		let img = `<img src="${path}" class="${extClass}"/>`;
		return img;
	});
}

let textToPath = (emoText, isRichText) => {
	let word = emoText.replace(/\#|\;/gi, '');
	let idx = emoTextList.indexOf(word);
	let path = `/static/emoji/${idx}.gif`;
	// #ifdef MP-WEIXIN	
	// 小程序的表情要去掉最前面"/"(但有的时候又不能去掉,十分奇怪)
	if (isRichText) {
		path = path.slice(1);
	}
	// #endif
	return path;
}

export default {
	containEmoji,
	emoTextList,
	transform,
	textToPath
}