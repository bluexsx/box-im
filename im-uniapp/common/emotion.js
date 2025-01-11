const emoTextList = ['憨笑', '媚眼', '开心', '坏笑', '可怜', '爱心', '笑哭', '拍手', '惊喜', '打气',
	'大哭', '流泪', '饥饿', '难受', '健身', '示爱', '色色', '眨眼', '暴怒', '惊恐',
	'思考', '头晕', '大吐', '酷笑', '翻滚', '享受', '鼻涕', '快乐', '雀跃', '微笑',
	'贪婪', '红心', '粉心', '星星', '大火', '眼睛', '音符', "叹号", "问号", "绿叶",
	"燃烧", "喇叭", "警告", "信封", "房子", "礼物", "点赞", "举手", "拍手", "点头",
	"摇头", "偷瞄", "庆祝", "疾跑", "打滚", "惊吓", "起跳"
];


let transform = (content, extClass) => {
	return content.replace(/\#[\u4E00-\u9FA5]{1,3}\;/gi, (emoText)=>{
		// 将匹配结果替换表情图片
		let word = emoText.replace(/\#|\;/gi, '');
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
	let word = emoText.replace(/\#|\;/gi, '');
	let idx = emoTextList.indexOf(word);
	return `/static/emoji/${idx}.gif`;
}



export default {
	emoTextList,
	transform,
	textToPath
}