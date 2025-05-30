<template>
	<div class="chat-input-area">
		<div :class="['edit-chat-container', isEmpty ? '' : 'not-empty']" contenteditable="true"
			@paste.prevent="onPaste" @keydown="onKeydown" @compositionstart="compositionFlag = true"
			@compositionend="onCompositionEnd" @input="onEditorInput" @mousedown="onMousedown" ref="content"
			@blur="onBlur">
		</div>
		<chat-at-box @select="onAtSelect" :search-text="atSearchText" ref="atBox" :ownerId="ownerId"
			:members="groupMembers"></chat-at-box>
	</div>
</template>

<script>
import ChatAtBox from "./ChatAtBox";

export default {
	name: "ChatInput",
	components: { ChatAtBox },
	props: {
		ownerId: {
			type: Number,
		},
		groupMembers: {
			type: Array,
		},
	},
	data() {
		return {
			imageList: [],
			fileList: [],
			currentId: 0,
			atSearchText: null,
			compositionFlag: false,
			atIng: false,
			isEmpty: true,
			changeStored: true,
			blurRange: null
		}
	},
	methods: {
		onPaste(e) {
			this.isEmpty = false;
			let txt = e.clipboardData.getData('Text')
			let range = window.getSelection().getRangeAt(0)
			if (range.startContainer !== range.endContainer || range.startOffset !== range.endOffset) {
				range.deleteContents();
			}
			// 粘贴图片和文件时，这里没有数据
			if (txt && typeof(txt) == 'string') {
				let textNode = document.createTextNode(txt);
				range.insertNode(textNode)
				range.collapse();
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
						let line = this.newLine();
						let imageElement = document.createElement('img');
						imageElement.className = 'chat-image no-text';
						imageElement.src = imagePush.url;
						imageElement.dataset.imgId = imagePush.fileId;
						line.appendChild(imageElement);
						let after = document.createTextNode('\u00A0');
						line.appendChild(after);
						this.selectElement(after, 1);
					} else {
						let asFile = items[i].getAsFile();
						if (!asFile) {
							continue;
						}
						let filePush = { fileId: this.generateId(), file: asFile };
						this.fileList[filePush.fileId] = (filePush)
						let line = this.newLine();
						let fileElement = this.createFile(filePush);
						line.appendChild(fileElement);
						let after = document.createTextNode('\u00A0');
						line.appendChild(after);
						this.selectElement(after, 1);
					}
				}
			}
			range.collapse();
		},
		selectElement(element, endOffset) {
			let selection = window.getSelection();
			// 插入元素可能不是立即执行的，vue可能会在插入元素后再更新dom
			this.$nextTick(() => {
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
		onCompositionEnd(e) {
			this.compositionFlag = false;
			this.onEditorInput(e);
		},
		onKeydown(e) {
			if (e.keyCode === 13) {
				e.preventDefault();
				e.stopPropagation();
				if (this.atIng) {
					this.$refs.atBox.select();
					return;
				}
				if (e.ctrlKey) {
					let line = this.newLine();
					let after = document.createTextNode('\u00A0');
					line.appendChild(after);
					this.selectElement(line.childNodes[0], 0);
				} else {
					// 中文输入标记
					if (this.compositionFlag) {
						return;
					}
					this.submit();
				}
				return;
			}
			// 删除键
			if (e.keyCode === 8) {
				// 等待dom更新
				setTimeout(() => {
					let s = this.$refs.content.innerHTML.trim();
					// 空dom时，需要刷新dom
					if (s === '' || s === '<br>' || s === '<div>&nbsp;</div>') {
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
			let endContainer = blurRange.endContainer
			let startOffset = endContainer.data.indexOf("@" + this.atSearchText);
			let endOffset = startOffset + this.atSearchText.length + 1;
			blurRange.setStart(blurRange.endContainer, startOffset);
			blurRange.setEnd(blurRange.endContainer, endOffset);
			blurRange.deleteContents()
			blurRange.collapse();
			this.focus();
			// 创建元素节点
			let element = document.createElement('SPAN')
			element.className = "chat-at-user";
			element.dataset.id = member.userId;
			element.contentEditable = 'false'
			element.innerText = `@${member.showNickName}`
			blurRange.insertNode(element)
			// 光标移动到末尾
			blurRange.collapse()

			// 插入空格
			let textNode = document.createTextNode('\u00A0');
			blurRange.insertNode(textNode);

			blurRange.collapse()
			this.atSearchText = "";
			this.selectElement(textNode, 1);
		},
		onEditorInput(e) {
			this.isEmpty = false;
			this.changeStored = false;
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

				let endIndex = endOffset;
				for (let i = endOffset; i < textContent.length; i++) {
					if (textContent[i] === ' ') {
						endIndex = i;
						break;
					}
				}
				this.atSearchText = textContent.substring(startIndex + 1, endIndex).trim();
				// 打开选择弹窗
				if (this.atSearchText == '') {
					this.showAtBox(e)
				}
			}

		},
		onBlur(e) {
			if (!this.atIng) {
				this.updateRange();
			}
		},
		onMousedown() {
			if (this.atIng) {
				this.$refs.atBox.close();
				this.atIng = false;
			}
		},
		insertEmoji(emojiText) {
			let emojiElement = document.createElement('img');
			emojiElement.className = 'emoji-normal no-text';
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
			icon.className = 'el-icon-document';
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
			if (parentElement.parentElement === this.$refs.content) {
				divElement.innerHTML = endContainer.textContent.substring(range.endOffset).trim();
				endContainer.textContent = endContainer.textContent.substring(0, range.endOffset);
				// 插入到当前div（当前行）后面
				parentElement.insertAdjacentElement('afterend', divElement);
			} else {
				divElement.innerHTML = '';
				this.$refs.content.append(divElement);
			}
			return divElement;
		},
		clear() {
			this.empty();
			this.imageList = [];
			this.fileList = [];
			this.$refs.atBox.close();
		},
		empty() {
			this.$refs.content.innerHTML = "";
			let line = this.newLine();
			let after = document.createTextNode('\u00A0');
			line.appendChild(after);
			this.$nextTick(() => this.selectElement(after));
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
		html2Escape(strHtml) {
			return strHtml.replace(/[<>&"]/g, function(c) {
				return {
					'<': '&lt;',
					'>': '&gt;',
					'&': '&amp;',
					'"': '&quot;'
				} [c];
			});
		},
		submit() {
			let nodes = this.$refs.content.childNodes;
			let fullList = [];
			let tempText = '';
			let atUserIds = [];
			let each = (nodes) => {
				for (let i = 0; i < nodes.length; i++) {
					let node = nodes[i];
					if (!node) {
						continue;
					}
					if (node.nodeType === 3) {
						tempText += this.html2Escape(node.textContent);
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
									content: text,
									atUserIds: atUserIds
								})
								tempText = '';
								atUserIds = []
							}
							fullList.push({
								type: 'image',
								content: this.imageList[imgId]
							})
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
									content: text,
									atUserIds: atUserIds
								})
								tempText = '';
								atUserIds = []
							}
							fullList.push({
								type: 'file',
								content: this.fileList[fileId]
							})
						} else {
							tempText += '\n';
							each(node.childNodes);
						}
					} else if (nodeName === 'span') {
						if (node.dataset.id) {
							tempText += node.innerHTML;
							atUserIds.push(node.dataset.id)
						} else if (node.outerHtml) {
							tempText += node.outerHtml;
						}
					}
				}
			}
			each(nodes)
			let text = tempText.trim();
			if (text !== '') {
				fullList.push({
					type: 'text',
					content: text,
					atUserIds: atUserIds
				})
			}
			this.$emit('submit', fullList);
		},
		focus() {
			this.$refs.content.focus();
		}

	}
}
</script>

<style lang="scss">
.chat-input-area {
	width: 100%;
	height: 100%;
	position: relative;

	.edit-chat-container {
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

		// 单独一行时，无法在前面输入的bug
		>div:before {
			content: "\00a0";
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
			background: #eeeC;
			border-radius: 10px;

			.file-position-left {
				display: flex;
				width: 80px;
				justify-content: center;
				align-items: center;

				.el-icon-document {
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

	.edit-chat-container>div:nth-of-type(1):after {
		content: '请输入消息（按Ctrl+Enter键换行）';
		color: gray;
	}

	.edit-chat-container.not-empty>div:nth-of-type(1):after {
		content: none;
	}

}
</style>