const emoTextList = ['微笑', '撇嘴', '色', '发呆', '得意', '流泪', '害羞', '闭嘴', '睡', '大哭', '尴尬', '发怒', '调皮', '呲牙', '惊讶', '难过', '酷',
	'冷汗', '抓狂', '吐', '偷笑', '可爱', '白眼', '傲慢', '饥饿', '困', '惊恐', '流汗', '憨笑', '大兵', '奋斗', '咒骂', '疑问', '嘘', '晕', '折磨',
	'衰', '骷髅', '敲打', '再见', '擦汗', '抠鼻', '鼓掌', '糗大了', '坏笑', '左哼哼', '右哼哼', '哈欠', '鄙视', '委屈', '快哭了', '阴险', '亲亲', '吓',
	'可怜', '菜刀', '西瓜', '啤酒', '篮球', '乒乓', '咖啡', '饭', '猪头', '玫瑰', '凋谢', '示爱', '爱心', '心碎', '蛋糕', '闪电', '炸弹', '刀', '足球',
	'瓢虫', '便便', '月亮', '太阳', '礼物', '拥抱', '强', '弱', '握手', '胜利', '抱拳', '勾引', '拳头', '差劲', '爱你', 'NO', 'OK', '爱情', '飞吻',
	'跳跳', '发抖', '怄火', '转圈', '磕头', '回头', '跳绳', '挥手', '激动', '街舞', '献吻', '左太极', '右太极'
];

let emoImageUrlList = [];

// 备注：经过测试，小程序的<rich-text>无法显示相对路径的图片，所以在这里对图片提前全部转成绝对路径
// 提前初始化图片的url
for (let i = 0; i < emoTextList.length; i++) {
	let path = `/static/emoji/${i}.gif`;
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
	let img = `<img src="${emoImageUrlList[idx]}" style="vertical-align:bottom;"/>`;
	return img;
}


let textToPath = (emoText) => {
	let word = emoText.replace(/\#|\;/gi, '');
	let idx = emoTextList.indexOf(word);
	return  `/static/emoji/${idx}.gif`;
}



export default {
	emoTextList,
	transform,
	textToImg,
	textToPath
}