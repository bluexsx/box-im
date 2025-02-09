let replaceURLWithHTMLLinks = (content, color) => {
	// 使用正则表达式匹配更广泛的URL格式
	const urlRegex = /(?:https?:\/\/[^\s]+|www\.[^\s]+)/g;
	return content.replace(urlRegex, (url) => {
	    // 如果URL不以http(s)://开头，则添加http://前缀
	    if (!url.startsWith("http")) {
	        url = "http://" + url;
	    }
	    return `<a href="${url}" target="_blank" style="color: ${color};text-decoration: underline;">${url}</a>`;
	});
}

export default {
	replaceURLWithHTMLLinks
}