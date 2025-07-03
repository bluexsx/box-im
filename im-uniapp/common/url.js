
// 使用正则表达式匹配更广泛的URL格式(此正则由deepseek生成)
const regex = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|]|\bwww\.[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
	
let containUrl = (content) => {
	return regex.test(content)
}


let replaceURLWithHTMLLinks = (content, color) => {
	return content.replace(regex, (url) => {
	    // 如果URL不以http(s)://开头，则添加http://前缀
	    if (!url.startsWith("http")) {
	        url = "http://" + url;
	    }
	    return `<a href="${url}" target="_blank" style="color: ${color};text-decoration: underline;">${url}</a>`;
	});
}

export default {
	containUrl,
	replaceURLWithHTMLLinks
}