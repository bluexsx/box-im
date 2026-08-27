const emoTextList = [
  '六六六',
  '微笑',
  '抱心',
  '捂脸',
  '点赞',
  '笑哭',
  '读书',
  '听歌',
  '期待',
  '飞吻',
  '吃瓜',
  '可怜',
  '惊讶',
  '生气',
  '困',
  '思考',
  '拜托',
  '怒火',
  '笑指',
  '疑惑',
  '书呆',
  '晕',
  '星眼',
  '伤心',
  '无语',
  '好的',
  '比耶',
  '呲牙',
  '奶茶',
  '放大',
  '大哭',
  '庆祝',
  '花痴',
  '闭嘴',
  '吐舌',
  '鼻涕',
  '爆头',
  '喊话',
  '吐彩',
  '嘘',
  '酷笑',
  '斜眼',
  '憨笑',
  '担心',
  '心动',
  '害羞',
  '祈祷',
  '土豪',
  '发财',
  '叹气',
  '紧张',
  '口罩',
  '鼓掌',
  '挥手',
  '耶',
  '比心',
  'OK',
  '指上',
  '指右',
  '摇滚',
  '合十',
  '碰拳',
  '握拳',
  '赞',
  '倒赞',
  '六',
  '大便',
  '蛋糕',
  '红包',
  '礼花',
  '咖啡',
  '西瓜',
  '月亮'
];

const EMOJI_REGEX = /\[(?:OK|[\u4E00-\u9FA5]{1,3})\]/gi;

const modules = import.meta.glob('@/assets/emoji/*.png', { eager: true, import: 'default' }) as Record<string, string>;

const emojiUrlMap = new Map<number, string>();
Object.keys(modules).forEach((path) => {
  const match = path.match(/\/(\d+)\.png$/);
  if (match) {
    emojiUrlMap.set(Number(match[1]), modules[path]);
  }
});

export const formatEmoji = (word: string) => `[${word}]`;

export const parseEmojiWord = (emoText: string) => {
  const match = String(emoText).match(/^\[(OK|[\u4E00-\u9FA5]{1,3})\]$/i);
  return match ? (match[1].toUpperCase() === 'OK' ? 'OK' : match[1]) : emoText;
};

export const textToUrl = (emoText: string) => {
  const word = parseEmojiWord(emoText);
  const idx = emoTextList.indexOf(word);
  if (idx === -1) {
    return '';
  }
  return emojiUrlMap.get(idx) || '';
};

// 将匹配结果替换表情图片
const textToImg = (emoText: string, extClass?: string) => {
  const word = parseEmojiWord(emoText);
  const idx = emoTextList.indexOf(word);
  if (idx === -1) {
    return emoText;
  }
  const url = emojiUrlMap.get(idx);
  if (!url) {
    return emoText;
  }
  return `<img src="${url}" class="${extClass || ''}" />`;
};

export const transform = (content: string, extClass?: string) => {
  return content.replace(EMOJI_REGEX, (text) => textToImg(text, extClass));
};

export const filterRecentEmojis = (list: string[]) => {
  return (list || []).filter((text) => emoTextList.includes(text));
};

export { emoTextList, EMOJI_REGEX };
