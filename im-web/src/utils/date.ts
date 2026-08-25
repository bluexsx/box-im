export const toTimeText = (timeStamp: number | string | Date, simple?: boolean) => {
  const dateTime = new Date(timeStamp);
  const currentTime = Date.parse(String(new Date()));
  const timeDiff = currentTime - dateTime.getTime();
  let timeText = '';
  if (timeDiff <= 60000) {
    timeText = '刚刚';
  } else if (timeDiff > 60000 && timeDiff < 3600000) {
    timeText = Math.floor(timeDiff / 60000) + '分钟前';
  } else if (timeDiff >= 3600000 && timeDiff < 86400000 && !isYestday(dateTime)) {
    timeText = formatDateTime(dateTime).substr(11, 5);
  } else if (isYestday(dateTime)) {
    timeText = '昨天' + ' ' + formatDateTime(dateTime).substr(11, 5);
  } else if (isYear(dateTime)) {
    timeText = formatDateTime(dateTime).substr(5, simple ? 5 : 14);
  } else {
    timeText = formatDateTime(dateTime);
    if (simple) {
      timeText = timeText.substr(2, 8);
    }
  }
  return timeText;
};

export const isToday = (date: Date) => {
  const today = new Date();
  return today.getFullYear() === date.getFullYear() && today.getMonth() === date.getMonth() && today.getDate() === date.getDate();
};

const isYestday = (date: Date) => {
  const yesterday = new Date(Date.now() - 1000 * 60 * 60 * 24);
  return yesterday.getFullYear() === date.getFullYear() && yesterday.getMonth() === date.getMonth() && yesterday.getDate() === date.getDate();
};

const isYear = (date: Date) => {
  return date.getFullYear() === new Date().getFullYear();
};

const formatDateTime = (date: Date | string | number | '') => {
  if (date === '' || !date) {
    return '';
  }
  const dateObject = new Date(date);
  const y = dateObject.getFullYear();
  let m: number | string = dateObject.getMonth() + 1;
  m = m < 10 ? '0' + m : m;
  let d: number | string = dateObject.getDate();
  d = d < 10 ? '0' + d : d;
  let h: number | string = dateObject.getHours();
  h = h < 10 ? '0' + h : h;
  let minute: number | string = dateObject.getMinutes();
  minute = minute < 10 ? '0' + minute : minute;
  let second: number | string = dateObject.getSeconds();
  second = second < 10 ? '0' + second : second;
  return y + '/' + m + '/' + d + ' ' + h + ':' + minute + ':' + second;
};
