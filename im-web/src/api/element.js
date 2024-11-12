let fixTop = (e) => {
	var offset = e.offsetTop
	if (e.offsetParent != null) {
		offset += fixTop(e.offsetParent)
	}
	return offset
}

let fixLeft = (e) => {
	var offset = e.offsetLeft
	if (e.offsetParent != null) {
		offset += fixLeft(e.offsetParent)
	}
	return offset
}

let setTitleTip = (tip) => {
	let title = process.env.VUE_APP_NAME;
	if(tip){
		title = `(${tip})${title}`;
	}
	document.title =title;
	
}

export default{
	fixTop,
	fixLeft,
	setTitleTip
}
