import Vue from 'vue'

// v-dialogDrag: 弹窗拖拽
Vue.directive('dialogDrag', {
    bind(el, binding, vnode, oldVnode) {
        const dialogHeaderEl = el.querySelector('.el-dialog__header')
        const dragDom = el.querySelector('.el-dialog')
        dialogHeaderEl.style.cursor = 'move'

        // 获取原有属性 ie dom元素.currentStyle 火狐谷歌 window.getComputedStyle(dom元素, null);
        const sty = dragDom.currentStyle || window.getComputedStyle(dragDom, null)

        dialogHeaderEl.onmousedown = (e) => {
            // 鼠标按下，计算当前元素距离可视区的距离
            const disX = e.clientX - dialogHeaderEl.offsetLeft
            const disY = e.clientY - dialogHeaderEl.offsetTop
            const screenWidth = document.body.clientWidth; // body当前宽度
            const screenHeight = document.documentElement.clientHeight; // 可见区域高度(应为body高度，可某些环境下无法获取)
            const dragDomWidth = dragDom.offsetWidth; // 对话框宽度
            const dragDomheight = dragDom.offsetHeight; // 对话框高度
            const minDragDomLeft = dragDom.offsetLeft;
            const maxDragDomLeft = screenWidth - dragDom.offsetLeft - dragDomWidth;
            const minDragDomTop = dragDom.offsetTop;
            const maxDragDomTop = screenHeight - dragDom.offsetTop - dragDomheight;

            // 获取到的值带px 正则匹配替换
            let styL, styT

            // 注意在ie中 第一次获取到的值为组件自带50% 移动之后赋值为px
            if (sty.left.includes('%')) {
                styL = +document.body.clientWidth * (+sty.left.replace(/\%/g, '') / 100)
                styT = +document.body.clientHeight * (+sty.top.replace(/\%/g, '') / 100)
            } else {
                styL = +sty.left.replace(/\px/g, '')
                styT = +sty.top.replace(/\px/g, '')
            }

            document.onmousemove = function (e) {
                // 获取body的页面可视宽高
                // var clientHeight = document.documentElement.clientHeight || document.body.clientHeight
                // var clientWidth = document.documentElement.clientWidth || document.body.clientWidth

                // 通过事件委托，计算移动的距离
                var l = e.clientX - disX
                var t = e.clientY - disY

                // 边界处理
                if (-l > minDragDomLeft) {
                    l = -minDragDomLeft;
                } else if (l > maxDragDomLeft) {
                    l = maxDragDomLeft;
                }
                if (-t > minDragDomTop) {
                    t = -minDragDomTop;
                } else if (t > maxDragDomTop) {
                    t = maxDragDomTop;
                }
                // 移动当前元素
                dragDom.style.left = `${l + styL}px`
                dragDom.style.top = `${t + styT}px`

                // 将此时的位置传出去
                // binding.value({x:e.pageX,y:e.pageY})
            }

            document.onmouseup = function (e) {
                document.onmousemove = null
                document.onmouseup = null
            }
        }
    }
})
