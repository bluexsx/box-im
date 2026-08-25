<template>
  <div
    class="chat-input-area"
    :class="{ 'is-dragover': isDragOver }"
    @dragenter.prevent="onDragEnter"
    @dragover.prevent="onDragOver"
    @dragleave.prevent="onDragLeave"
    @drop.prevent="onDrop">
    <div v-show="isEmpty && !isDragOver" class="input-placeholder">{{ '请输入消息（按Ctrl+Enter键换行）' }}</div>
    <div v-show="isDragOver" class="drag-mask">
      <span>{{ '松开鼠标以添加文件' }}</span>
    </div>
    <div
      ref="contentRef"
      :class="['edit-container', isEmpty ? '' : 'not-empty']"
      contenteditable="true"
      @paste.prevent="onPaste"
      @keydown="onKeydown"
      @compositionstart="compositionFlag = true"
      @compositionend="onCompositionEnd"
      @input="onEditorInput"
      @mousedown="onMousedown"
      @keyup="onKeyup"
      @click="onClickInput" />
    <ChatAtBox ref="atBoxRef" :search-text="atSearchText || ''" :group="group!" :members="groupMembers" @select="onAtSelect" />
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref } from 'vue';
import ChatAtBox from '@/components/chat/ChatAtBox.vue';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';
import { EMOJI_REGEX, emoTextList, parseEmojiWord, textToUrl } from '@/utils/emotion';

type ImagePush = {
  fileId: number;
  file: File;
  url: string;
};

type FilePush = {
  fileId: number;
  file: File;
};

type SubmitTextItem = {
  type: 'text';
  content: string;
  atUserIds: string[];
};

type SubmitImageItem = {
  type: 'image';
  content: ImagePush;
};

type SubmitFileItem = {
  type: 'file';
  content: FilePush;
};

export type ChatInputSubmitItem = SubmitTextItem | SubmitImageItem | SubmitFileItem;

const props = defineProps({
  group: {
    type: Object as () => GroupVO,
    default: undefined
  },
  groupMembers: {
    type: Array as () => GroupMemberVO[],
    default: () => []
  }
});

const emit = defineEmits<{
  submit: [fullList: ChatInputSubmitItem[]];
}>();

const contentRef = ref<HTMLDivElement>();
const atBoxRef = ref<InstanceType<typeof ChatAtBox>>();
const imageList = ref<Record<number, ImagePush>>({});
const fileList = ref<Record<number, FilePush>>({});
const currentId = ref(0);
const atSearchText = ref<string>();
const compositionFlag = ref(false);
const atIng = ref(false);
const isEmpty = ref(true);
const blurRange = ref<Range>();
const isDragOver = ref(false);
const dragCounter = ref(0);

const insertPastedTextWithEmoji = (range: Range, txt: string) => {
  // 将粘贴文本中的表情编码 [xxx] 转为表情图片插入
  const emojiPattern = new RegExp(EMOJI_REGEX.source, 'g');
  const parts: Array<{ type: 'text' | 'emoji'; content: string }> = [];
  let lastIndex = 0;
  let match: RegExpExecArray | null;
  while ((match = emojiPattern.exec(txt)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ type: 'text', content: txt.substring(lastIndex, match.index) });
    }
    const code = match[0];
    const word = parseEmojiWord(code);
    if (emoTextList.indexOf(word) !== -1) {
      parts.push({ type: 'emoji', content: code });
    } else {
      parts.push({ type: 'text', content: code });
    }
    lastIndex = match.index + code.length;
  }
  if (lastIndex < txt.length) {
    parts.push({ type: 'text', content: txt.substring(lastIndex) });
  }
  if (parts.length === 0) {
    parts.push({ type: 'text', content: txt });
  }
  for (let i = 0; i < parts.length; i++) {
    const part = parts[i];
    let node: Node;
    if (part.type === 'text') {
      node = document.createTextNode(part.content);
    } else {
      const img = document.createElement('img');
      img.className = 'emoji-normal no-text';
      img.dataset.emojiCode = part.content;
      img.src = textToUrl(part.content);
      node = img;
    }
    range.insertNode(node);
    if (i < parts.length - 1) {
      range.setStartAfter(node);
      range.setEndAfter(node);
    }
  }
  range.collapse(false);
};

const onPaste = (e: ClipboardEvent) => {
  isEmpty.value = false;
  const txt = e.clipboardData?.getData('Text') || '';
  const selection = window.getSelection();
  if (!selection?.rangeCount) {
    return;
  }
  const range = selection.getRangeAt(0);
  if (range.startContainer !== range.endContainer || range.startOffset !== range.endOffset) {
    range.deleteContents();
  }
  // 粘贴图片和文件时，这里没有数据
  if (txt && typeof txt === 'string') {
    insertPastedTextWithEmoji(range, txt);
    return;
  }
  const items = e.clipboardData?.items;
  if (items?.length) {
    for (let i = 0; i < items.length; i++) {
      const asFile = items[i].getAsFile();
      if (!asFile) {
        continue;
      }
      insertMediaFile(asFile);
    }
  }
  range.collapse();
};

const onDragEnter = (e: DragEvent) => {
  if (!hasDragFiles(e)) {
    return;
  }
  dragCounter.value++;
  isDragOver.value = true;
};

const onDragOver = (e: DragEvent) => {
  if (!hasDragFiles(e)) {
    return;
  }
  isDragOver.value = true;
};

const onDragLeave = () => {
  dragCounter.value = Math.max(0, dragCounter.value - 1);
  if (dragCounter.value === 0) {
    isDragOver.value = false;
  }
};

const onDrop = (e: DragEvent) => {
  dragCounter.value = 0;
  isDragOver.value = false;
  const files = e.dataTransfer?.files;
  if (!files?.length) {
    return;
  }
  focus();
  nextTick(() => {
    const selection = window.getSelection();
    if (!selection?.rangeCount) {
      if (blurRange.value) {
        selection?.addRange(blurRange.value);
      } else {
        moveCursorToEnd();
      }
    }
    for (let i = 0; i < files.length; i++) {
      insertMediaFile(files[i]);
    }
  });
};

const hasDragFiles = (e: DragEvent) => {
  const types = e.dataTransfer?.types;
  if (!types) {
    return false;
  }
  return Array.from(types).includes('Files');
};

const insertMediaFile = (file: File) => {
  if (!file) {
    return;
  }
  isEmpty.value = false;
  if (file.type && file.type.indexOf('image') !== -1) {
    const imagePush: ImagePush = {
      fileId: generateId(),
      file,
      url: URL.createObjectURL(file)
    };
    imageList.value[imagePush.fileId] = imagePush;
    const line = newLine();
    const imageElement = document.createElement('img');
    imageElement.className = 'chat-image no-text';
    imageElement.src = imagePush.url;
    imageElement.dataset.imgId = String(imagePush.fileId);
    line.appendChild(imageElement);
    const after = document.createTextNode('\u00A0');
    line.appendChild(after);
    selectElement(after, 1);
  } else {
    const filePush: FilePush = { fileId: generateId(), file };
    fileList.value[filePush.fileId] = filePush;
    const line = newLine();
    const fileElement = createFile(filePush);
    line.appendChild(fileElement);
    const after = document.createTextNode('\u00A0');
    line.appendChild(after);
    selectElement(after, 1);
  }
};

const pasteScreenShot = (buffer: Blob) => {
  const id = generateId();
  const file = new File([buffer], `screenShot-${id}.png`, { type: 'image/png' }); //type为图片的格式
  const imagePush: ImagePush = {
    fileId: id,
    file,
    url: URL.createObjectURL(file)
  };
  imageList.value[imagePush.fileId] = imagePush;
  const line = newLine();
  const imageElement = document.createElement('img');
  imageElement.className = 'chat-image no-text';
  imageElement.src = imagePush.url;
  imageElement.dataset.imgId = String(imagePush.fileId);
  line.appendChild(imageElement);
  const after = document.createTextNode('\u00A0');
  line.appendChild(after);
  selectElement(after, 1);
};

const selectElement = (element: Node, endOffset?: number) => {
  // 插入元素可能不是立即执行的，vue可能会在插入元素后再更新dom
  nextTick(() => {
    const selection = window.getSelection();
    const t1 = document.createRange();
    t1.setStart(element, 0);
    t1.setEnd(element, endOffset || 0);
    if (element.firstChild) {
      t1.selectNodeContents(element.firstChild);
    }
    t1.collapse();
    selection?.removeAllRanges();
    selection?.addRange(t1);
    // 需要时自动聚焦
    if ('focus' in element && typeof (element as HTMLElement).focus === 'function') {
      (element as HTMLElement).focus();
    }
  });
};

const onCompositionEnd = () => {
  compositionFlag.value = false;
  onEditorInput();
};

const onKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    e.preventDefault();
    e.stopPropagation();
    if (atIng.value) {
      atBoxRef.value?.select();
      return;
    }
    if (e.ctrlKey) {
      const selection = window.getSelection();
      if (!selection?.rangeCount) {
        return;
      }
      const currentRange = selection.getRangeAt(0);
      const currentContainer = currentRange.endContainer;
      // 如果光标在零宽度空格节点中，先移动到父节点，避免影响 newLine() 的识别
      if (currentContainer.nodeType === 3 && currentContainer.textContent === '\u200B') {
        const parent = currentContainer.parentNode;
        if (parent) {
          const offset = Array.from(parent.childNodes).indexOf(currentContainer as ChildNode);
          currentRange.setStart(parent, offset);
          currentRange.setEnd(parent, offset);
          currentRange.collapse(true);
          selection.removeAllRanges();
          selection.addRange(currentRange);
        }
      }
      const line = newLine();
      // 使用零宽度空格节点（\u200B）用于光标定位，不可见，不影响对齐
      const zeroWidthSpace = document.createTextNode('\u200B');
      line.appendChild(zeroWidthSpace);
      // 同步设置光标位置，确保连续换行时能正确定位
      const range = document.createRange();
      range.setStart(zeroWidthSpace, 0);
      range.setEnd(zeroWidthSpace, 0);
      range.collapse(true);
      selection.removeAllRanges();
      selection.addRange(range);
      // 同步更新 blurRange，确保下次操作使用正确位置
      blurRange.value = range.cloneRange();
      isEmpty.value = false;
    } else {
      // 中文输入标记
      if (compositionFlag.value) {
        return;
      }
      submit();
    }
    return;
  }
  // 删除键
  if (e.key === 'Backspace') {
    // 等待dom更新
    setTimeout(() => {
      const s = contentRef.value?.innerHTML.trim() || '';
      // 空dom时，需要刷新dom
      if (s === '' || s === '<br>' || s === '<div>&nbsp;</div>') {
        // 拼接随机长度的空格，以刷新dom
        empty();
        isEmpty.value = true;
        if (contentRef.value) {
          selectElement(contentRef.value);
        }
      } else {
        isEmpty.value = false;
      }
    });
  }
  // at框打开时，上下键移动特殊处理
  if (atIng.value) {
    if (e.key === 'ArrowUp') {
      e.preventDefault();
      e.stopPropagation();
      atBoxRef.value?.moveUp();
    }
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      e.stopPropagation();
      atBoxRef.value?.moveDown();
    }
  }
};

const insertAtMember = (member: GroupMemberVO) => {
  focus();
  nextTick(() => {
    updateRange();
    const range = blurRange.value;
    if (!range) {
      return;
    }
    if (range.startContainer !== range.endContainer || range.startOffset !== range.endOffset) {
      range.deleteContents();
    }
    const element = document.createElement('SPAN');
    element.className = 'chat-at-user';
    element.dataset.id = String(member.userId);
    element.contentEditable = 'false';
    element.innerText = `@${member.showNickName}`;
    range.insertNode(element);
    range.collapse(false);
    const textNode = document.createTextNode('\u00A0');
    range.insertNode(textNode);
    range.collapse();
    selectElement(textNode, 1);
    isEmpty.value = false;
  });
};

const onAtSelect = (member: GroupMemberVO) => {
  atIng.value = false;
  // 选中输入的 @xx 符
  const range = blurRange.value;
  if (!range) {
    return;
  }
  const endContainer = range.endContainer;
  if (endContainer.nodeType !== 3 || atSearchText.value == null) {
    return;
  }
  const startOffset = endContainer.textContent?.indexOf('@' + atSearchText.value) ?? -1;
  const endOffset = startOffset + atSearchText.value.length + 1;
  range.setStart(range.endContainer, startOffset);
  range.setEnd(range.endContainer, endOffset);
  range.deleteContents();
  range.collapse();
  focus();
  // 创建元素节点
  const element = document.createElement('SPAN');
  element.className = 'chat-at-user';
  element.dataset.id = String(member.userId);
  element.contentEditable = 'false';
  element.innerText = `@${member.showNickName}`;
  range.insertNode(element);
  // 光标移动到末尾
  range.collapse();
  // 插入空格
  const textNode = document.createTextNode('\u00A0');
  range.insertNode(textNode);
  range.collapse();
  atSearchText.value = '';
  selectElement(textNode, 1);
};

const onEditorInput = () => {
  isEmpty.value = false;
  if (props.groupMembers?.length && !compositionFlag.value) {
    const selection = window.getSelection();
    if (!selection?.rangeCount) {
      return;
    }
    const range = selection.getRangeAt(0);
    // 截取@后面的名称作为过滤条件，并以空格结束
    const endContainer = range.endContainer;
    const endOffset = range.endOffset;
    const textContent = endContainer.textContent || '';
    let startIndex = -1;
    for (let i = endOffset; i >= 0; i--) {
      if (textContent[i] === '@') {
        startIndex = i;
        break;
      }
    }
    // 没有at符号，则关闭弹窗
    if (startIndex === -1) {
      atBoxRef.value?.close();
      return;
    }
    let endIndex = endOffset;
    for (let i = endOffset; i < textContent.length; i++) {
      if (textContent[i] === ' ') {
        endIndex = i;
        break;
      }
    }
    atSearchText.value = textContent.substring(startIndex + 1, endIndex).trim();
    // 打开选择弹窗
    if (atSearchText.value === '') {
      showAtBox();
    }
  }
};

const onClickInput = () => {
  updateRange();
};

const onKeyup = () => {
  updateRange();
};

const onMousedown = () => {
  if (atIng.value) {
    atBoxRef.value?.close();
    atIng.value = false;
  }
};

const insertEmoji = (emojiText: string) => {
  const emojiElement = document.createElement('img');
  emojiElement.className = 'emoji-normal no-text';
  emojiElement.dataset.emojiCode = emojiText;
  emojiElement.src = textToUrl(emojiText);
  let range = blurRange.value;
  if (!range) {
    focus();
    updateRange();
    range = blurRange.value;
  }
  if (!range) {
    return;
  }
  if (range.startContainer !== range.endContainer || range.startOffset !== range.endOffset) {
    range.deleteContents();
  }
  range.insertNode(emojiElement);
  range.collapse();
  const textNode = document.createTextNode('\u200B');
  range.insertNode(textNode);
  range.collapse();
  selectElement(textNode);
  isEmpty.value = false;
};

const insertEmotion = (html: string) => {
  if (!html) {
    return;
  }
  if (html.startsWith('<')) {
    let range = blurRange.value;
    if (!range) {
      focus();
      updateRange();
      range = blurRange.value;
    }
    if (!range || !contentRef.value) {
      return;
    }
    if (range.startContainer !== range.endContainer || range.startOffset !== range.endOffset) {
      range.deleteContents();
    }
    const temp = document.createElement('div');
    temp.innerHTML = html;
    const nodes = Array.from(temp.childNodes);
    nodes.forEach((node) => {
      range!.insertNode(node);
      range!.collapse(false);
    });
    const textNode = document.createTextNode('\u200B');
    range.insertNode(textNode);
    range.collapse();
    selectElement(textNode);
    isEmpty.value = false;
    return;
  }
  insertEmoji(html);
};

const generateId = () => {
  return currentId.value++;
};

const createFile = (filePush: FilePush) => {
  const file = filePush.file;
  const fileId = filePush.fileId;
  const container = document.createElement('div');
  container.className = 'chat-file-container no-text';
  container.contentEditable = 'false';
  container.dataset.fileId = String(fileId);
  const left = document.createElement('div');
  left.className = 'file-position-left';
  container.appendChild(left);
  const icon = document.createElement('div');
  icon.className = 'icon iconfont icon-message-file';
  left.appendChild(icon);
  const right = document.createElement('div');
  right.className = 'file-position-right';
  container.appendChild(right);
  const fileName = document.createElement('div');
  fileName.className = 'file-name';
  fileName.innerText = file.name;
  const fileSize = document.createElement('div');
  fileSize.className = 'file-size';
  fileSize.innerText = sizeConvert(file.size);
  right.appendChild(fileName);
  right.appendChild(fileSize);
  return container;
};

const sizeConvert = (len: number) => {
  if (len < 1024) {
    return len + 'B';
  }
  if (len < 1024 * 1024) {
    return (len / 1024).toFixed(2) + 'KB';
  }
  if (len < 1024 * 1024 * 1024) {
    return (len / 1024 / 1024).toFixed(2) + 'MB';
  }
  return (len / 1024 / 1024 / 1024).toFixed(2) + 'GB';
};

const updateRange = () => {
  const selection = window.getSelection();
  if (selection?.rangeCount) {
    blurRange.value = selection.getRangeAt(0);
  }
};

const newLine = () => {
  const selection = window.getSelection();
  if (!selection?.rangeCount || !contentRef.value) {
    const divElement = document.createElement('div');
    contentRef.value?.append(divElement);
    return divElement;
  }
  const range = selection.getRangeAt(0);
  const divElement = document.createElement('div');
  const endContainer = range.endContainer;
  const parentElement = endContainer.parentElement;
  if (parentElement?.parentElement === contentRef.value) {
    divElement.innerHTML = endContainer.textContent?.substring(range.endOffset).trim() || '';
    endContainer.textContent = endContainer.textContent?.substring(0, range.endOffset) || '';
    // 插入到当前div（当前行）后面
    parentElement.insertAdjacentElement('afterend', divElement);
  } else {
    divElement.innerHTML = '';
    contentRef.value.append(divElement);
  }
  return divElement;
};

const clear = () => {
  empty();
  imageList.value = {};
  fileList.value = {};
  atBoxRef.value?.close();
};

const empty = () => {
  if (!contentRef.value) {
    return;
  }
  contentRef.value.innerHTML = '';
  const line = newLine();
  const after = document.createTextNode('\u00A0');
  line.appendChild(after);
  nextTick(() => selectElement(after));
};

const showAtBox = () => {
  atIng.value = true;
  // show之后会自动更新当前搜索的text
  const selection = window.getSelection();
  if (!selection?.rangeCount) {
    return;
  }
  const range = selection.getRangeAt(0);
  // 光标所在坐标
  const pos = range.getBoundingClientRect();
  atBoxRef.value?.open({ x: pos.x, y: pos.y });
  // 记录光标所在位置
  updateRange();
};

const parseContent = (): ChatInputSubmitItem[] => {
  const nodes = contentRef.value?.childNodes;
  const fullList: ChatInputSubmitItem[] = [];
  let tempText = '';
  let atUserIds: string[] = [];
  const each = (nodeList: NodeListOf<ChildNode>) => {
    for (let i = 0; i < nodeList.length; i++) {
      const node = nodeList[i];
      if (!node) {
        continue;
      }
      if (node.nodeType === 3) {
        tempText += node.textContent;
        continue;
      }
      const nodeName = node.nodeName.toLowerCase();
      if (nodeName === 'script') {
        continue;
      }
      const text = tempText.trim();
      if (nodeName === 'img') {
        const imgEl = node as HTMLImageElement;
        const imgId = imgEl.dataset.imgId;
        if (imgId) {
          if (text) {
            fullList.push({ type: 'text', content: text, atUserIds: [...atUserIds] });
            tempText = '';
            atUserIds = [];
          }
          fullList.push({ type: 'image', content: imageList.value[Number(imgId)] });
        } else {
          tempText += imgEl.dataset.emojiCode || '';
        }
      } else if (nodeName === 'div') {
        const divEl = node as HTMLDivElement;
        const fileId = divEl.dataset.fileId;
        // 文件
        if (fileId) {
          if (text) {
            fullList.push({ type: 'text', content: text, atUserIds: [...atUserIds] });
            tempText = '';
            atUserIds = [];
          }
          fullList.push({ type: 'file', content: fileList.value[Number(fileId)] });
        } else {
          tempText += '\n';
          each(node.childNodes);
        }
      } else if (nodeName === 'span') {
        const spanEl = node as HTMLSpanElement;
        if (spanEl.dataset.id) {
          tempText += spanEl.innerHTML;
          atUserIds.push(spanEl.dataset.id);
        } else {
          tempText += spanEl.outerHTML;
        }
      }
    }
  };
  if (nodes) {
    each(nodes);
  }
  const text = tempText.replace(/\u200B/g, '').trim();
  if (text !== '') {
    fullList.push({ type: 'text', content: text, atUserIds: [...atUserIds] });
  }
  return fullList;
};

const submit = () => {
  emit('submit', parseContent());
};

const focus = () => {
  contentRef.value?.focus();
};

const getContent = () => {
  return parseContent();
};

const moveCursorToEnd = () => {
  const content = contentRef.value;
  if (!content) {
    return;
  }
  const range = document.createRange();
  const selection = window.getSelection();
  // 将光标设置到 content 元素的末尾
  range.selectNodeContents(content);
  // false表示折叠到范围的末尾
  range.collapse(false);
  selection?.removeAllRanges();
  selection?.addRange(range);
  focus();
  updateRange();
};

defineExpose({
  focus,
  clear,
  insertEmotion,
  insertEmoji,
  getContent,
  submit,
  insertAtMember,
  pasteScreenShot
});
</script>

<style lang="scss">
.chat-input-area {
  width: 100%;
  height: 100%;
  position: relative;

  &.is-dragover {
    .edit-container {
      outline: 2px dashed #587ff0;
      outline-offset: -2px;
      background: rgba(88, 127, 240, 0.06);
    }
  }

  .drag-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;
    pointer-events: none;
    color: #587ff0;
    font-size: var(--im-font-size);
    font-weight: 600;
    background: rgba(88, 127, 240, 0.08);
  }

  .input-placeholder {
    position: absolute;
    top: 5px;
    left: 5px;
    color: gray;
    pointer-events: none;
    font-size: var(--im-font-size);
    line-height: 26px;
  }

  .edit-container {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    outline: none;
    padding: 5px;
    line-height: 26px;
    font-size: var(--im-font-size);
    text-align: left;
    overflow-y: auto;
    white-space: pre-wrap;
    // 单独一行时，无法在前面输入的bug

    > div::before {
      content: '\00a0';
      font-size: 14px;
      position: absolute;
      top: 0;
      left: 0;
    }

    .chat-image {
      display: block;
      max-width: 200px;
      max-height: 100px;
      border: 1px solid #e6e6e6;
      cursor: pointer;
    }

    .chat-file-container {
      max-width: 65%;
      padding: 10px;
      border: 2px solid #587ff0;
      display: flex;
      background: #eeec;
      border-radius: 10px;

      .file-position-left {
        display: flex;
        width: 80px;
        justify-content: center;
        align-items: center;

        .icon-message-file {
          font-size: 40px;
          text-align: center;
          color: #d42e07;
        }
      }

      .file-position-right {
        flex: 1;

        .file-name {
          font-size: 16px;
          font-weight: 600;
          color: #66b1ff;
        }

        .file-size {
          font-size: 14px;
          font-weight: 600;
        }
      }
    }

    .chat-at-user {
      color: #00f;
      border-radius: 3px;
    }
  }
}
</style>
