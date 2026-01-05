<template>
    <div class="h-screen max-w-3xl mx-auto relative">
        <!-- 聊天记录区域 -->
        <div class="overflow-y-auto pb-24 pt-4 px-4">
            <!-- 聊天记录区域 -->
             <div class="overflow-y-auto pb-24 pt-4 px-4">
                <!-- 遍历聊天记录 -->
                 <template v-for="(chat, index) in chatList" :key="index">
                    <!-- 用户提问消息（靠右） -->
                    <div v-if="chat.role === 'user'" class="flex justify-end mb-4">
                        <div class="question-container">
                            <p>{{ chat.content }}</p>
                        </div>
                    </div>
                    
                    <!-- 大模型回复消息（靠左） -->
                    <div v-else class="flex mb-4">
                        <!-- 头像 -->
                        <div class="flex-shrink-0 mr-3">
                            <div class="w-8 h-8 rounded-full flex items-center justify-center border border-gray-200">
                                <SvgIcon name="deepseek-logo" customCss="w-5 h-5"></SvgIcon>
                            </div>
                        </div>
                        <!-- 回复的内容 -->
                        <div class="p-1 max-w-[80%] mb-2">
                            <p>{{ chat.content }}</p>
                        </div>
                    </div>
                 </template>
             </div>
        </div>
        <!-- 提问输入框 -->
        <div class="absolute bottom-0 left-0 w-full mb-5">
            <div class="bg-gray-100 rounded-3xl px-4 py-3 mx-4 border border-gray-200">
                <textarea 
                    v-model="message"
                    placeholder="询问任何问题"
                    class="bg-transparent border-none outline-none w-full text-sm resize-none min-h-[24px]"
                    rows="2"
                    @input="autoResize"
                    @keydown.enter.prevent="sendMessage"
                    ref="textareaRef"
                    >
                </textarea>

                <!-- 发送按钮 -->
                <div class="flex justify-end">
                    <button 
                    @click="sendMessage" 
                    :disabled="!message.trim()"
                    class="flex items-center justify-center bg-[#4d6bfe] rounded-full w-8 h-8 border border-[#4d6bfe] hover:bg-[#3b5bef] transition-colors
                    disable:opacity-50
                    disable:cursor-not-allowed">
                        <SvgIcon name="up-arrow" customCss="w-5 h-5 text-white"></SvgIcon>
                    </button>
                </div>
            </div>
            <!-- 提示文字 -->
            <div class="flex items-center justify-center text-xs text-gray-400 mt-2">内容由 AI 生成，请仔细甄别</div>
        </div>
    </div>
</template>

<script setup>
import SvgIcon from '@/components/SvgIcon.vue';
import { ref, onBeforeUnmount } from 'vue';

// 输入的消息
const message = ref('')

// textarea 引用
const textareaRef = ref(null);

// 自动调整文本域高度
const autoResize = () => {
    const textarea = textareaRef.value;
    if (textarea) { // 若文本域存在
        textarea.style.height = 'auto'; // 1. 先将高度重置为 'auto'
        textarea.style.height = textarea.scrollHeight + 'px'; // 2. 再设置为内容的实际高度
    }
}

// 聊天记录（给个默认的问候语）
const chatList = ref([
    { role: 'assistant', content: '我是智能 AI 助手！✨ 我可以帮你解答各种问题，无论是学习、工作，还是日常生活中的小困惑，都可以找我聊聊。有什么我可以帮你的吗？😊' }
])

// SSE 连接
let eventSource = null;

// 发送消息
const sendMessage = async () => {
    // 校验发送的消息不能为空
    if (!message.value.trim()) return

    // 将用户发送的消息添加到 chatList 聊天列表中
    const userMessage = message.value.trim()
    chatList.value.push({ role: 'user', content: userMessage })

    // 点击发送按钮后，清空输入框
    message.value = ''
    // 将输入框的高度重置
    if (textareaRef.value) {
        textareaRef.value.style.height = 'auto'
    }

    // 添加一个占位的回复消息
    chatList.value.push({ role: 'assistant', content: '' })

    try {
        // 建立 SSE 连接
        eventSource = new EventSource(`http://localhost:8081/v6/ai/generateStream?message=${encodeURIComponent(userMessage)}`)
        // 响应的回答
        let responseText = ''

        // 处理消息事件
        eventSource.onmessage = (event) => {
            console.log('接收到数据：', event.data)
            if (event.data) {  // 若响应数据不为空
                // 持续追加流失回答
                responseText += event.data

                // 更新最后一条消息
                chatList.value[chatList.value.length - 1].content = responseText;
            }
        }

        // 处理错误
        eventSource.onerror = (error) => {
            // 通常 SSE 在完成传输后会触发一次 error 事件，这是正常的
            if (error.eventPhase === EventSource.CLOSED) {
                console.log('SSE正常关闭')
            } else {
                // 提示用户“请求出错”
                chatList.value[chatList.value.length - 1].content = '抱歉，请求出错了，请稍后重试。'
            }

            // 关闭 SSE
            closeSSE()
        }
    } catch (error) {
        console.error('发送消息错误：', error)
        // 提示用户“请求出错”
        chatList.value[chatList.value.length - 1].content = '抱歉，请求出错了，请稍后重试。';
    }
}

const closeSSE = () => {
    if (eventSource) {
        eventSource.close()
        eventSource = null
    }
}

onBeforeUnmount(() => {
    closeSSE()
})

</script>

<style scoped>
.question-container {
    font-size: 16px;
    line-height: 28px;
    color: #262626;
    padding: calc((44px - 28px) / 2) 20px;
    box-sizing: border-box;
    white-space: pre-wrap;
    word-break: break-word;
    background-color: #eff6ff;
    border-radius: 14px;
    max-width: calc(100% - 48px);
    position: relative;
}
</style>