import pinia from './index.js'
import useChatStore from './chatStore.js'
import useFriendStore from './friendStore.js'
import useGroupStore from './groupStore.js'
import useConfigStore from './configStore.js'
import useUserStore from './userStore.js'

export const chatStore = useChatStore(pinia)
export const friendStore = useFriendStore(pinia)
export const groupStore = useGroupStore(pinia)
export const configStore = useConfigStore(pinia)
export const userStore = useUserStore(pinia)
