const emoTextList = ['憨笑', '媚眼', '开心', '坏笑', '可怜', '爱心', '笑哭', '拍手', '惊喜', '打气',
	'大哭', '流泪', '饥饿', '难受', '健身', '示爱', '色色', '眨眼', '暴怒', '惊恐',
	'思考', '头晕', '大吐', '酷笑', '翻滚', '享受', '鼻涕', '快乐', '雀跃', '微笑',
	'贪婪', '红心', '粉心', '星星', '大火', '眼睛', '音符', "叹号", "问号", "绿叶",
	"燃烧", "喇叭", "警告", "信封", "房子", "礼物", "点赞", "举手", "拍手", "点头",
	"摇头", "偷瞄", "庆祝", "疾跑", "打滚", "惊吓", "起跳"
];


let emoImageUrlList = [];

// 备注：经过测试，小程序的<rich-text>无法显示相对路径的图片，所以在这里对图片提前全部转成绝对路径
// 提前初始化图片的url
for (let i = 0; i < emoTextList.length; i++) {
	let path = `/static/emoji2/${i}.gif`;
	uni.getImageInfo({
		src: path,
		success(res) {
			emoImageUrlList[i] = res.path
		},
		fail(res) {
			emoImageUrlList = path;
		}
	});
}


let transform = (content) => {
	return content.replace(/\#[\u4E00-\u9FA5]{1,3}\;/gi, textToImg);
}



// 将匹配结果替换表情图片
let textToImg = (emoText) => {
	let word = emoText.replace(/\#|\;/gi, '');
	let idx = emoTextList.indexOf(word);
	let path = textToPath(emoText);
	// #ifdef MP
	// 微信小程序不能有前面的'/'
	path = path.slice(1);
	// #endif
	let img = `<img src="${path}" style="with:35px;height:35px;
	margin: 0 -2px;vertical-align:bottom;"/>`;
	return img;
}


let textToPath = (emoText) => {
	let word = emoText.replace(/\#|\;/gi, '');
	let idx = emoTextList.indexOf(word);
	return `/static/emoji/${idx}.gif`;
}



export default {
	emoTextList,
	transform,
	textToImg,
	textToPath
}