<template>
  <div class="chat-widget">
    <!-- 展开的聊天窗口 -->
    <transition name="cw-fade">
      <div v-if="open" class="cw-panel">
        <div class="cw-header">
          <span class="cw-title">智能客服</span>
          <span class="cw-close" @click="open = false">×</span>
        </div>

        <div class="cw-body" ref="bodyRef">
          <div v-if="messages.length === 0" class="cw-empty">
            你好，我是酒店智能客服 👋<br />请问有什么可以帮您？
          </div>
          <div
            v-for="(m, i) in messages"
            :key="i"
            class="cw-msg"
            :class="m.role === 'user' ? 'is-user' : 'is-bot'"
          >
            <div class="cw-bubble">{{ m.content }}</div>
          </div>
          <div v-if="loading" class="cw-msg is-bot">
            <div class="cw-bubble cw-typing">正在输入…</div>
          </div>
        </div>

        <div class="cw-input">
          <input
            v-model="input"
            type="text"
            placeholder="请输入您的问题…"
            :disabled="loading"
            @keyup.enter="send"
          />
          <button :disabled="loading || !input.trim()" @click="send">发送</button>
        </div>
      </div>
    </transition>

    <!-- 悬浮按钮 -->
    <button v-if="!open" class="cw-fab" @click="open = true" title="在线客服">💬</button>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { sendChatMessage } from '@/api/chat'

const open = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([])
const bodyRef = ref(null)

// 每次重新登录（登录态变化）时清空历史提问，避免看到上一个账号的对话
watch(
  () => localStorage.getItem('token'),
  (newVal, oldVal) => {
    if (newVal !== oldVal) {
      messages.value = []
      input.value = ''
      loading.value = false
      open.value = false
    }
  }
)

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const payload = messages.value.map(m => ({ role: m.role, content: m.content }))
    const res = await sendChatMessage(payload)
    const reply = res.data || '抱歉，我暂时无法回答这个问题。'
    messages.value.push({ role: 'assistant', content: reply })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，客服系统暂时不可用，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chat-widget {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
}

.cw-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #409eff, #2f7bff);
  color: #fff;
  font-size: 26px;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}

.cw-fab:hover {
  transform: scale(1.06);
}

.cw-panel {
  width: 340px;
  height: 480px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.cw-header {
  height: 48px;
  background: linear-gradient(135deg, #409eff, #2f7bff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.cw-title {
  font-size: 15px;
  font-weight: 600;
}

.cw-close {
  font-size: 22px;
  cursor: pointer;
  line-height: 1;
}

.cw-body {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background: #f5f7fa;
}

.cw-empty {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin-top: 60px;
  line-height: 1.8;
}

.cw-msg {
  display: flex;
  margin-bottom: 12px;
}

.cw-msg.is-user {
  justify-content: flex-end;
}

.cw-msg.is-bot {
  justify-content: flex-start;
}

.cw-bubble {
  max-width: 78%;
  padding: 9px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.is-user .cw-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 2px;
}

.is-bot .cw-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
  border-bottom-left-radius: 2px;
}

.cw-typing {
  color: #909399;
}

.cw-input {
  display: flex;
  border-top: 1px solid #ebeef5;
  padding: 8px;
  background: #fff;
}

.cw-input input {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 13px;
  outline: none;
}

.cw-input input:focus {
  border-color: #409eff;
}

.cw-input button {
  margin-left: 8px;
  padding: 0 16px;
  border: none;
  border-radius: 6px;
  background: #409eff;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.cw-input button:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}

.cw-fade-enter-active,
.cw-fade-leave-active {
  transition: all 0.25s ease;
}

.cw-fade-enter-from,
.cw-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

@media (max-width: 576px) {
  .cw-panel {
    width: calc(100vw - 32px);
    height: 60vh;
  }

  .chat-widget {
    right: 16px;
    bottom: 16px;
  }
}
</style>
