<template>
  <div class="chat-input-area">
    <div class="edit-chat-container" contenteditable="true"
         @paste.prevent="onPaste"
         @keydown="onKeydown"
         @compositionstart="compositionFlag=true"
         @compositionend="compositionFlag=false"
         @input="onEditorInput"
         @mousedown="onMousedown"
         v-html="contentHtml"
         ref="content"
         @blur="onBlur"

         :placeholder="'请输入消息'">
    </div>
    <chat-at-box @select="onAtSelect"
                 :search-text="atSearchText"
                 ref="atBox"
                 :ownerId="ownerId" :members="groupMembers"

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
      focusOffset: 0,
      atIng: false,
      blurRange: null
    }
  }, methods: {
    onPaste(e) {
      let txt = e.clipboardData.getData('Text')
      let range = window.getSelection().getRangeAt(0)
      if (range.startContainer !== range.endContainer || range.startOffset !== range.endOffset) {
        document.execCommand('delete', false, null);
      }
      if (typeof (txt) == 'string') {
        let textNode = document.createTextNode(txt);
        range.insertNode(textNode)
        // return;
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


            let imageElement = document.createElement('img');
            imageElement.className = 'chat-image no-text';
            imageElement.src = imagePush.url;
            imageElement.setAttribute("data-img-id", imagePush.fileId);
            // imageElement.width = 200;
            // imageElement.height = 100;
            range.insertNode(imageElement);
          } else {
            let asFile = items[i].getAsFile();
            if (!asFile) {
              continue;
            }
            let filePush = {fileId: this.generateId(), file: asFile};
            this.fileList[filePush.fileId] = (filePush)
            let fileElement = this.createFile(filePush);
            range.insertNode(fileElement);

            // var text = document.createTextNode("\u00A0");
            fileElement.insertAdjacentHTML('afterend','\u00A0');
            // range.selectNodeContents(text);
            // range.collapse();
          }
        }
      }
      range.collapse();

    },
    selectElement(element) {
      let selection = window.getSelection();

      setTimeout(() => {
        let t1 = document.createRange();
        t1.setStart(element, 0);
        t1.setEnd(element, 0);
        if (element.firstChild) {
          t1.selectNodeContents(element.firstChild);
        }
        t1.collapse();

        selection.removeAllRanges();
        selection.addRange(t1);
        element.focus();
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
          let selection = window.getSelection();
          let range = selection.getRangeAt(0);

          let divElement = document.createElement('div');

          let endContainer = range.endContainer;
          let parentElement = endContainer.parentElement;

          let newText = endContainer.textContent.substring(range.endOffset).trim();
          endContainer.textContent = endContainer.textContent.substring(0, range.endOffset);
          divElement.innerHTML = newText || '';

          if (parentElement === this.$refs.content) {
            // range.insertNode(divElement)
            this.$refs.content.append(divElement);
          } else {
            parentElement.insertAdjacentElement('afterend', divElement);
          }

          // if (range.startContainer !== endContainer || range.startOffset !== range.endOffset) {
          //   console.log('delete range.', range.startContainer, range.startOffset, range.endContainer, range.endOffset)
          //   document.execCommand('delete', false, null);
          // }
          this.selectElement(divElement);
          // return false;
        } else {
          if (this.compositionFlag) {
            console.log('中文输入中')
            return;
          }
          this.submit();
        }
        return;
      }
      if (e.keyCode === 90) {
        if (e.ctrlKey || e.metaKey) {
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
      if (e.keyCode === 8) {
        setTimeout(() => {
          // console.log(this.$refs.content.innerHTML.trim())
          let s = this.$refs.content.innerHTML.trim();
          if (s === '' || s === '<br>') {
            // 拼接随机长度的空格，以刷新dom
            this.empty();
            this.selectElement(this.$refs.content);
          }
          // this.
        })
      }
      // console.log(e.keyCode)
      if (this.atIng) {
        console.log('atIng', e.keyCode)
        e.preventDefault();
        e.stopPropagation();
        if (e.keyCode === 38) {
          this.$refs.atBox.moveUp();
        }
        if (e.keyCode === 40) {
          this.$refs.atBox.moveDown();
        }
      }
    },
    onAtSelect(member) {
      this.atIng = false;
      let range = window.getSelection().getRangeAt(0)
      // 选中输入的 @xx 符
      range.setStart(this.blurRange.startContainer, this.focusOffset - 1 - this.atSearchText.length)
      range.setEnd(this.blurRange.endContainer, this.focusOffset)
      range.deleteContents()
      // 创建元素节点
      let element = document.createElement('SPAN')
      element.className = "chat-at-user";
      element.dataset.id = member.userId;
      element.contentEditable = 'false'
      element.innerText = `@${member.aliasName}`
      element.setAttribute("data-at-user-id", member.userId)
      range.insertNode(element)
      // 光标移动到末尾
      range.collapse()
      // 插入空格
      let textNode = document.createTextNode('\u00A0');
      range.insertNode(textNode)
      range.selectNodeContents(textNode);

      range.collapse()
      this.atSearchText = "";
      this.focus();
      // this.selectElement(textNode);
      // this.$refs.editBox.focus()
    },
    onEditorInput(e) {
      // 如果触发 @
      if (this.$props.groupMembers && !this.zhLock) {
        if (e.data === '@') {
          // 打开选择弹窗
          this.showAtBox(e);
        } else {
          let selection = window.getSelection()
          // let range = selection.getRangeAt(0)
          let focusNode = selection.focusNode;
          // 截取@后面的名称作为过滤条件
          let stIdx = focusNode.textContent.lastIndexOf('@');
          this.atSearchText = focusNode.textContent.substring(stIdx + 1);
        }
      }

    },
    onBlur(e) {
      // setTimeout(() => {
      //   console.log(e)
      //   this.$refs.atBox.close();
      //   this.atIng = false;
      // },100)
      this.blurRange = window.getSelection().getRangeAt(0);
    },
    onMousedown() {
      if (this.atIng) {
        this.$refs.atBox.close();
        this.atIng = false;
      }
    },
    insertEmoji(emojiText) {
      // let selection = window.getSelection();
      // let range = selection.getRangeAt(0);
      let emojiElement = document.createElement('img');
      emojiElement.className = 'chat-emoji no-text';
      emojiElement.setAttribute("data-emoji-code", emojiText);
      emojiElement.src = this.$emo.textToUrl(emojiText);

      let blurRange = this.blurRange;
      if (blurRange.startContainer !== blurRange.endContainer || blurRange.startOffset !== blurRange.endOffset) {
        blurRange.deleteContents();
      }
      blurRange.insertNode(emojiElement);

      this.focus();
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
      container.setAttribute("data-file-id", fileId);

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
      this.atSearchText = "";
      let selection = window.getSelection()
      let range = selection.getRangeAt(0)
      // 记录光标所在位置
      // this.focusNode = selection.focusNode;
      this.focusOffset = selection.focusOffset;
      // 光标所在坐标
      let pos = range.getBoundingClientRect();
      this.$refs.atBox.open({
        x: pos.x,
        y: pos.y
      })
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
            let imgId = node.getAttribute('data-img-id');
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
              let emojiCode = node.getAttribute('data-emoji-code');
              tempText += emojiCode;
            }
          } else if (nodeName === 'div') {
            let fileId = node.getAttribute('data-file-id');
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
            let userId = node.getAttribute("data-at-user-id");
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
    console.log(this.$props.groupMembers)
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
    }

    > div:before {
      content: "\00a0";
      font-size: 14px;
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

  .edit-chat-container:empty:before {
    content: '请输入消息（按Shift+Enter键换行）';
    color: gray;
  }

  .edit-chat-container:focus:before {
    content: none;
  }

}
</style>