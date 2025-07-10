let html2Escape = (strText) => {
	return strText.replace(/[<>&"]/g, function(c) {
		return {
			'<': '&lt;',
			'>': '&gt;',
			'&': '&amp;',
			'"': '&quot;'
		} [c];
	});
}

export default {
	html2Escape
}