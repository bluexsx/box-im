import { defineMixin } from '../../libs/vue'
import defProps from '../../libs/config/props.js'
import transitionConfig from './transition'

// H5 下 index.js 通过 import.meta.glob 预加载组件时，可能与 props.js 形成循环依赖，
// 导致 defProps.transition 尚未初始化，需回退到本地默认配置
const getTransitionProps = () => defProps.transition || transitionConfig.transition

export const props = defineMixin({
    props: {
        // 是否展示组件
        show: {
            type: Boolean,
            default: () => getTransitionProps().show
        },
        // 使用的动画模式
        mode: {
            type: String,
            default: () => getTransitionProps().mode
        },
        // 动画的执行时间，单位ms
        duration: {
            type: [String, Number],
            default: () => getTransitionProps().duration
        },
        // 使用的动画过渡函数
        timingFunction: {
            type: String,
            default: () => getTransitionProps().timingFunction
        }
    }
})
