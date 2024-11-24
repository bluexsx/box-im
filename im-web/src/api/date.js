let toTimeText = (timeStamp, simple) => {
	var dateTime = new Date(timeStamp)
	var currentTime = Date.parse(new Date()); //当前时间
	var timeDiff = currentTime - dateTime; //与当前时间误差
	var timeText = '';
	if (timeDiff <= 60000) { //一分钟内
		timeText = '刚刚';
	} else if (timeDiff > 60000 && timeDiff < 3600000) {
		//1小时内
		timeText = Math.floor(timeDiff / 60000) + '分钟前';
	} else if (timeDiff >= 3600000 && timeDiff < 86400000 && !isYestday(dateTime)) {
		//今日
		timeText = formatDateTime(dateTime).substr(11, 5);
	} else if (isYestday(dateTime)) {
		//昨天
		timeText = '昨天' + formatDateTime(dateTime).substr(11, 5);
	} else if (isYear(dateTime)) {
		//今年
		timeText = formatDateTime(dateTime).substr(5, simple ? 5 : 14);
	} else {
		//不属于今年
		timeText = formatDateTime(dateTime);
		if (simple) {
			timeText = timeText.substr(2, 8);
		}
	}
	return timeText;
}

let isYestday = (date) => {
	var yesterday = new Date(new Date() - 1000 * 60 * 60 * 24);
	return yesterday.getYear() === date.getYear() &&
		yesterday.getMonth() === date.getMonth() &&
		yesterday.getDate() === date.getDate();
}

let isYear = (date) => {
	return date.getYear() === new Date().getYear();
}

let formatDateTime = (date) => {
	if (date === '' || !date) {
		return ''
	}
	var dateObject = new Date(date)
	var y = dateObject.getFullYear()
	var m = dateObject.getMonth() + 1
	m = m < 10 ? ('0' + m) : m
	var d = dateObject.getDate()
	d = d < 10 ? ('0' + d) : d
	var h = dateObject.getHours()
	h = h < 10 ? ('0' + h) : h
	var minute = dateObject.getMinutes()
	minute = minute < 10 ? ('0' + minute) : minute
	var second = dateObject.getSeconds()
	second = second < 10 ? ('0' + second) : second
	return y + '/' + m + '/' + d + ' ' + h + ':' + minute + ':' + second
}


export {
	toTimeText,
	isYestday,
	isYear,
	formatDateTime
}