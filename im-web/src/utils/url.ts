/** 将文本中的 URL 替换为可点击链接 */
export const replaceURLWithHTMLLinks = (content: string, color = '') => {
  // 使用正则表达式匹配更广泛的URL格式
  const urlRegex = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]|\bwww\.[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|])/gi;
  return content.replace(urlRegex, (url) => {
    let href = url;
    // 如果URL不以http(s)://开头，则添加http://前缀
    if (!url.startsWith('http')) {
      href = 'http://' + url;
    }
    return `<a href="${href}" target="_blank" style="color: ${color};text-decoration: underline;">${url}</a>`;
  });
};
