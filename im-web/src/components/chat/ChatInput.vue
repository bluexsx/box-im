<template>
  <div class="chat-input-area">
    <div :class="['edit-chat-container',isEmpty?'':'not-empty']" contenteditable="true"
         @paste.prevent="onPaste"
         @keydown="onKeydown"
         @compositionstart="compositionFlag=true"
         @compositionend="compositionFlag=false;updateRange()"
         @input="onEditorInput"
         @mousedown="onMousedown"
         v-html="contentHtml"
         ref="content"
         @blur="onBlur"
    >
    </div>
    <chat-at-box @select="onAtSelect"
                 :search-text="atSearchText"
                 ref="atBox"
                 :ownerId="ownerId"
                 :members="groupMembers"

    ></chat-at-box>
  </div>
</template>

<script>
import ChatAtBox from "./ChatAtBox";

export default {
  name: "ChatInput",
  components: {ChatAtBox},
  props: {
    ownerId: {
      type: Number,

    },
    groupMembers: {
      type: Array,
    },
  },
  data() {
    const defaultContentHtml = '<div></div>';
    return {
      // contentList: [],
      // content: null,
      defaultContentHtml,
      contentHtml: defaultContentHtml + '',
      imageList: [],
      fileList: [],
      currentId: 0,
      atSearchText: null,
      compositionFlag: false,
      history: [defaultContentHtml],
      atIng: false,
      isEmpty: true,
      blurRange: null
    }
  }, methods: {
    onPaste(e) {
      let txt = e.clipboardData.getData('Text')
      let range = window.getSelection().getRangeAt(0)

      if (range.startContainer !== range.endContainer || range.startOffset !== range.endOffset) {
        range.deleteContents();
      }
      // 粘贴图片和文件时，这里没有数据
      if (txt && typeof (txt) == 'string') {
        let textNode = document.createTextNode(txt);
        range.insertNode(textNode)
        return;
      }
      let items = (e.clipboardData || window.clipboardData).items
      if (items.length) {
        for (let i = 0; i < items.length; i++) {
          if (items[i].type.indexOf('image') !== -1) {
            let file = items[i].getAsFile();
            let imagePush = {
              fileId: this.generateId(),
              file: file,
              url: URL.createObjectURL(file)
            };
            this.imageList[imagePush.fileId] = (imagePush);

            let divElement = this.newLine();
            let text = document.createTextNode('\u00A0');
            divElement.appendChild(text);

            let imageElement = document.createElement('img');
            imageElement.className = 'chat-image no-text';
            imageElement.src = imagePush.url;
            imageElement.dataset.imgId = imagePush.fileId;

            divElement.appendChild(imageElement);
            let after = document.createTextNode('\u00A0');
            divElement.appendChild(after);
            this.selectElement(after, 1);
            // range.insertNode(divElement);
          } else {
            let asFile = items[i].getAsFile();
            if (!asFile) {
              continue;
            }
            let filePush = {fileId: this.generateId(), file: asFile};
            this.fileList[filePush.fileId] = (filePush)
            let line = this.newLine();
            let text = document.createTextNode('\u00A0');
            line.appendChild(text);

            let fileElement = this.createFile(filePush);
            line.appendChild(fileElement);

            let after = document.createTextNode('\u00A0');
            line.appendChild(after);
            this.selectElement(after, 1);
            // fileElement.insertAdjacentHTML('afterend', '\u00A0');
          }
        }
      }
      range.collapse();

    },
    selectElement(element, endOffset) {
      let selection = window.getSelection();
      // 插入元素可能不是立即执行的，vue可能会在插入元素后再更新dom
      setTimeout(() => {
        let t1 = document.createRange();
        t1.setStart(element, 0);
        t1.setEnd(element, endOffset || 0);
        if (element.firstChild) {
          t1.selectNodeContents(element.firstChild);
        }
        t1.collapse();

        selection.removeAllRanges();
        selection.addRange(t1);
        // 需要时自动聚焦
        if (element.focus) {
        element.focus();
        }
      })
    },
    onKeydown(e) {
      if (e.keyCode === 13) {
        e.preventDefault();
        e.stopPropagation();
        if (this.atIng) {
          console.log('选中at的人')
          this.$refs.atBox.select();
          return;
        }
        if (e.shiftKey) {
          let divElement = this.newLine();

          this.selectElement(divElement);
        } else {
          // 中文输入标记
          if (this.compositionFlag) {
            return;
          }
          this.submit();
        }
        return;
      }
      if (e.keyCode === 90) {
        // Ctrl+Z，这里兼容mac的command+z
        if (e.ctrlKey || e.metaKey) {
          // 阻止默认的ctrl+z，浏览器的ctrl+z很low
          e.preventDefault();
          e.stopPropagation();
          if (this.history.length <= 1) {
            return;
          }
          this.history.pop();
          let last = this.history.pop();
          // console.log('回滚为：', last);
          this.contentHtml = last;
          if (this.history.length === 0) {
            // 保底
            this.history.push(this.defaultContentHtml);
          }

          setTimeout(() => {
            let t = this.$refs.content.lastElementChild.lastChild;
            // t.focus();
            let r = document.createRange();
            r.setStart(t, t.textContent.length);
            r.setEnd(t, t.textContent.length);
            r.collapse();
            // r.selectNodeContents(t.lastChild);

            let selection = document.getSelection();
            selection.removeAllRanges();
            selection.addRange(r);
          })
        }
      }
      // 删除键
      if (e.keyCode === 8) {
        // 等待dom更新
        setTimeout(() => {
          let s = this.$refs.content.innerHTML.trim();
          // 空dom时，需要刷新dom
          if (s === '' || s === '<br>' || s === '<div><br></div>' || s === '<div><br/></div>') {
            // 拼接随机长度的空格，以刷新dom
            this.empty();
            this.isEmpty = true;
            this.selectElement(this.$refs.content);
          } else {
            this.isEmpty = false;
          }
        })
      }
      // at框打开时，上下键移动特殊处理
      if (this.atIng) {
        if (e.keyCode === 38) {
        e.preventDefault();
        e.stopPropagation();
          this.$refs.atBox.moveUp();
        }
        if (e.keyCode === 40) {
          e.preventDefault();
          e.stopPropagation();
          this.$refs.atBox.moveDown();
        }
      }
    },
    onAtSelect(member) {
      this.atIng = false;

      // 选中输入的 @xx 符
      let blurRange = this.blurRange;
      let startOffset = blurRange.endOffset - this.atSearchText.length - 1;
      blurRange.setStart(blurRange.endContainer, startOffset);
      blurRange.deleteContents()
      blurRange.collapse();

      this.focus();
      // 创建元素节点
      let element = document.createElement('SPAN')
      element.className = "chat-at-user";
      element.dataset.id = member.userId;
      element.contentEditable = 'false'
      element.innerText = `@${member.aliasName}`
      blurRange.insertNode(element)
      // 光标移动到末尾
      blurRange.collapse()

      // 插入空格
      let textNode = document.createTextNode('\u00A0');
      blurRange.insertNode(textNode);

      blurRange.collapse()
      this.atSearchText = "";
      this.selectElement(textNode);
    },
    onEditorInput(e) {
      // 加timeout是为了先响应compositionend事件
      this.isEmpty = false;
      setTimeout(() => {
        if (this.$props.groupMembers && !this.compositionFlag) {
          let selection = window.getSelection()
          let range = selection.getRangeAt(0);
          // 截取@后面的名称作为过滤条件，并以空格结束
          let endContainer = range.endContainer;
          let endOffset = range.endOffset;
          let textContent = endContainer.textContent;
          let startIndex = -1;
          for (let i = endOffset; i >= 0; i--) {
            if (textContent[i] === '@') {
              startIndex = i;
              break;
            }
          }
          // 没有at符号，则关闭弹窗
          if (startIndex === -1) {
            this.$refs.atBox.close();
            return;
          }
          // 打开选择弹窗
          this.showAtBox(e)
          let endIndex = endOffset;
          for (let i = endOffset; i < textContent.length; i++) {
            if (textContent[i] === ' ') {
              endIndex = i;
              break;
        }
      }
          this.atSearchText = textContent.substring(startIndex + 1, endIndex).trim();
        }
      })
    },
    onBlur(e) {
      this.updateRange();
    },
    onMousedown() {
      if (this.atIng) {
        this.$refs.atBox.close();
        this.atIng = false;
      }
    },
    insertEmoji(emojiText) {
      let emojiElement = document.createElement('img');
      emojiElement.className = 'chat-emoji no-text';
      emojiElement.dataset.emojiCode = emojiText;
      emojiElement.src = this.$emo.textToUrl(emojiText);

      let blurRange = this.blurRange;
      if (!blurRange) {
        this.focus();
        this.updateRange();
        blurRange = this.blurRange;
      }
      if (blurRange.startContainer !== blurRange.endContainer || blurRange.startOffset !== blurRange.endOffset) {
        blurRange.deleteContents();
      }
      blurRange.insertNode(emojiElement);
      blurRange.collapse()

      let textNode = document.createTextNode('\u00A0');
      blurRange.insertNode(textNode)
      blurRange.collapse()

      this.selectElement(textNode);
      this.updateRange();
      this.isEmpty = false;
    },
    generateId() {
      return this.currentId++;
    },
    createFile(filePush) {
      let file = filePush.file;
      let fileId = filePush.fileId;
      let container = document.createElement('div');
      container.className = 'chat-file-container no-text';
      container.contentEditable = 'false';
      container.dataset.fileId = fileId;

      let left = document.createElement('div');
      left.className = 'file-position-left';
      container.appendChild(left);

      let icon = document.createElement('div');
      icon.className = 'file-icon';
      icon.innerText = '?';
      left.appendChild(icon);

      let right = document.createElement('div');
      right.className = 'file-position-right';
      container.appendChild(right);

      let fileName = document.createElement('div');
      fileName.className = 'file-name';
      fileName.innerText = file.name;

      let fileSize = document.createElement('div');
      fileSize.className = 'file-size';
      fileSize.innerText = this.sizeConvert(file.size);

      right.appendChild(fileName);
      right.appendChild(fileSize);

      return container;
    },
    sizeConvert(len) {
      if (len < 1024) {
        return len + 'B';
      } else if (len < 1024 * 1024) {
        return (len / 1024).toFixed(2) + 'KB';
      } else if (len < 1024 * 1024 * 1024) {
        return (len / 1024 / 1024).toFixed(2) + 'MB';
      } else {
        return (len / 1024 / 1024 / 1024).toFixed(2) + 'GB';
      }
    },
    updateRange() {
      let selection = window.getSelection();
      this.blurRange = selection.getRangeAt(0);
    },
    newLine() {
      let selection = window.getSelection();
      let range = selection.getRangeAt(0);

      let divElement = document.createElement('div');

      let endContainer = range.endContainer;
      let parentElement = endContainer.parentElement;

      let newText = endContainer.textContent.substring(range.endOffset).trim();
      endContainer.textContent = endContainer.textContent.substring(0, range.endOffset);
      divElement.innerHTML = newText || '';

      // 有时候at完或者插入图片、文件后，可能parent可能直接是编辑器本身了，这里直接追加就可以了，可能有bug，但在所难免
      if (parentElement === this.$refs.content) {
        this.$refs.content.append(divElement);
      } else {
        // 插入到当前div（当前行）后面
        parentElement.insertAdjacentElement('afterend', divElement);
      }
      this.isEmpty = false;
      return divElement;
    },
    clear() {
      this.empty();
      this.imageList = [];
      this.fileList = [];
    },
    empty() {
      let emptyCount = Math.random() * 100 + 5;
      let content = '';
      for (let i = 0; i < emptyCount; i++) {
        content += ' ';
      }
      this.contentHtml = this.defaultContentHtml + content;
    },
    showAtBox(e) {
      this.atIng = true;
      // show之后会自动更新当前搜索的text
      // this.atSearchText = "";
      let selection = window.getSelection()
      let range = selection.getRangeAt(0)
      // 光标所在坐标
      let pos = range.getBoundingClientRect();
      this.$refs.atBox.open({
        x: pos.x,
        y: pos.y
      })
      // 记录光标所在位置
      this.updateRange();
    },
    submit() {
      // console.log(this.content)
      let nodes = this.$refs.content.childNodes;
      let textList = [];
      let imageList = [];
      let fileList = [];

      let fullList = [];


      let tempText = '';
      let each = (nodes) => {
        for (let i = 0; i < nodes.length; i++) {
          let node = nodes[i];
          if (!node) {
            continue;
          }
          if (node.nodeType === 3) {
            tempText += node.textContent;
            continue;
          }
          let nodeName = node.nodeName.toLowerCase();
          if (nodeName === 'script') {
            continue;
          }
          let text = tempText.trim();
          if (nodeName === 'img') {
            let imgId = node.dataset.imgId;
            if (imgId) {
              if (text) {
                fullList.push({
                  type: 'text',
                  content: text
                })
              }
              fullList.push({
                type: 'image',
                content: this.imageList[imgId]
              })
              imageList.push(this.imageList[imgId]);
              textList.push(text);
              tempText = '';
            } else {
              let emojiCode = node.dataset.emojiCode;
              tempText += emojiCode;
            }
          } else if (nodeName === 'div') {
            let fileId = node.dataset.fileId
            // 文件
            if (fileId) {
              if (text) {
                fullList.push({
                  type: 'text',
                  content: text
                })
              }
              fullList.push({
                type: 'file',
                content: this.fileList[fileId]
              })
              fileList.push(this.fileList[fileId]);
              textList.push(text);
              tempText = '';
            } else {
              tempText += '\n';
              each(node.childNodes);
            }
          } else if (nodeName === 'span') {
            let userId = node.dataset.id;
            if (userId !== null && userId !== undefined) {
              tempText += node.outerHTML;
            }
          } else {
            console.warn('未处理的标签');
            if (getComputedStyle(node).display === 'block') {
              tempText += '\n';
            } else {
              tempText += ' ';
            }
            each(node.childNodes);
          }
        }
      }
      each(nodes)
      let text = tempText.trim();
      if (text !== '') {
        fullList.push({
          type: 'text',
          content: text
        })
        textList.push(text);
      }
      // console.log(textList, imageList, fileList)

      this.$emit('submit', fullList, textList, imageList, fileList);
    },
    focus() {
      this.$refs.content.focus();
    }

  },
  mounted() {
    // console.log(this.$props.groupMembers)
    // this.$refs.content.firstElementChild.focus();
    this.selectElement(this.$refs.content.firstElementChild);
    setInterval(() => {
      if (!this.$refs.content) {
        return;
      }
      // 输入中文时不记录
      if (this.compositionFlag) {
        return;
      }
      let last = this.history[this.history.length];
      let newContent = this.$refs.content.innerHTML;
      if (last !== newContent) {
        this.history.push(newContent);
      }
    }, 1000);
  }
}
</script>

<style lang="scss">
.chat-input-area {
  width: 100%;
  height: 100%;
  position: relative;

  .edit-chat-container {
    //width: 100%;
    //height: 100%;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border: 1px solid #c3c3c3;
    outline: none;
    padding: 10px 0;
    line-height: 30px;
    font-size: 16px;
    text-align: left;

    > div {
      padding-left: 10px;
      //width: 1px;
      height: 30px;
    }

    // 单独一行时，无法在前面输入的bug
    > div:before {
      content: "\00a0";
      font-size: 14px;
      position: absolute;
      top: 0;
      left: 0;
    }

    .chat-image {
      max-width: 200px;
      max-height: 100px;
      border: 1px solid #e6e6e6;
      cursor: pointer;
      //margin-left: 10px;
    }

    .chat-emoji {
      width: 30px;
      height: 30px;
      vertical-align: top;
      cursor: pointer;
    }

    .chat-file-container {
      width: 200px;
      //height: 100px;
      padding: 5px 7px;
      border: 1px solid #4cd964;
      display: flex;

      .file-position-left {
        width: 30%;
        flex: 1;
        justify-content: center;
        align-items: center;

        .file-icon {
          width: 30px;
          height: 30px;
          text-align: center;
          justify-content: center;
          align-items: center;
          vertical-align: center;
          border: 1px solid #e6e6e6;
        }
      }

      .file-position-right {
        width: 70%;
        flex: 3;

        .file-name {
          font-size: 14px;
          color: #333;
        }

        .file-size {
          font-size: 12px;
          color: #666;
        }
      }
    }

    .chat-at-user {
      background-color: #4cd964;
      padding: 2px 5px;
      color: white;
      //border: 1px solid #c3c3c3;
      border-radius: 3px;
    }
  }

  .edit-chat-container > div:nth-of-type(1):empty:after {
    content: '请输入消息（按Shift+Enter键换行）';
    color: gray;
  }

  .edit-chat-container > div:nth-of-type(1):focus:after {
    content: none;
  }

  .edit-chat-container.not-empty > div:nth-of-type(1):after {
    content: none;
  }

}
</style>