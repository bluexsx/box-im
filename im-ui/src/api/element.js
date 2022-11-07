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

export default{
	fixTop,
	fixLeft
}
