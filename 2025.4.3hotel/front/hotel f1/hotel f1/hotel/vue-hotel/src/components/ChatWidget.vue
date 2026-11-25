<template>
  <div class="chat-widget">
    <!-- 展开的聊天窗口 -->
    <transition name="cw-fade">
      <div v-if="open" class="cw-panel">
        <div class="cw-header">
          <span class="cw-title">逸云智能客服 <em class="cw-badge">RAG</em></span>
          <div class="cw-header-actions">
            <span v-if="messages.length" class="cw-clear" title="清空对话" @click="clearMessages">清空</span>
            <span class="cw-close" @click="open = false">×</span>
          </div>
        </div>

        <div class="cw-body" ref="bodyRef" @scroll="onBodyScroll">
          <div v-if="messages.length === 0" class="cw-empty">
            <div class="cw-welcome">你好，我是逸云酒店智能客服 👋</div>
            <div class="cw-sub">支持实时查询房型空房与酒店政策，点击下方常见问题快速体验：</div>
            <div class="cw-quick-list">
              <button
                v-for="q in quickQuestions"
                :key="q"
                class="cw-quick-btn"
                :disabled="loading"
                @click="askQuick(q)"
              >{{ q }}</button>
            </div>
          </div>
          <div
            v-for="(m, i) in messages"
            :key="i"
            class="cw-msg"
            :class="m.role === 'user' ? 'is-user' : 'is-bot'"
          >
            <div class="cw-stack">
              <div class="cw-bubble">
                <template v-if="m.content">{{ m.content }}</template>
                <span v-else class="cw-typing">正在检索酒店资料…</span>
              </div>
              <div v-if="m.references && m.references.length" class="cw-refs">
                <div class="cw-refs-title">参考来源（{{ m.references.length }}）</div>
                <div v-for="(r, ri) in m.references" :key="ri" class="cw-ref">
                  <div class="cw-ref-head">
                    <span class="cw-ref-doc">{{ r.docTitle }}</span>
                    <span class="cw-ref-score">{{ Math.round((r.score || 0) * 100) }}%</span>
                  </div>
                  <div class="cw-ref-snippet">{{ r.snippet }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <button v-if="!atBottom" class="cw-scroll-down" title="回到最新" @click="scrollToBottom(true)">↓</button>

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
const atBottom = ref(true)

const quickQuestions = [
  '一家三口住什么房合适？',
  '现在还有哪些空房？',
  '入住押金要交多少钱？',
  '早餐几点开始供应？',
  '取消订单会扣费吗？'
]

function askQuick(q) {
  input.value = q
  send()
}

function clearMessages() {
  if (loading.value) return
  messages.value = []
}

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
  scrollToBottom(true)

  const payload = messages.value.map(m => ({ role: m.role, content: m.content }))
  messages.value.push({ role: 'assistant', content: '', references: [] })
  // 取响应式代理，保证流式追加文本能触发渲染
  const assistant = messages.value[messages.value.length - 1]

  try {
    await streamReply(payload, assistant)
    if (!assistant.content) {
      throw new Error('empty stream')
    }
  } catch (e) {
    // 流式失败时降级到普通接口，保证可用
    try {
      const res = await sendChatMessage(payload)
      const data = res.data || {}
      assistant.content = typeof data === 'string' ? data : (data.answer || '抱歉，我暂时无法回答这个问题。')
      assistant.references = (data && data.references) || []
    } catch (e2) {
      assistant.content = '抱歉，客服系统暂时不可用，请稍后再试或致电前台 8888。'
    }
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

/**
 * 消费后端 SSE：event: references / token / done / error
 */
async function streamReply(payload, assistant) {
  const resp = await fetch('/api/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'text/event-stream' },
    body: JSON.stringify({ messages: payload })
  })
  if (!resp.ok || !resp.body) {
    throw new Error('stream http ' + resp.status)
  }

  const reader = resp.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let eventName = ''
  let dataLines = []

  const dispatch = () => {
    if (!dataLines.length) {
      eventName = ''
      return
    }
    const raw = dataLines.join('\n')
    const name = eventName
    eventName = ''
    dataLines = []

    let data = null
    try {
      data = JSON.parse(raw)
    } catch (e) {
      data = raw
    }

    if (name === 'token') {
      assistant.content += (data && data.text) || ''
      scrollToBottom()
    } else if (name === 'references') {
      assistant.references = Array.isArray(data) ? data : []
      scrollToBottom()
    } else if (name === 'error') {
      throw new Error((data && data.message) || '服务异常')
    }
  }

  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    let index
    while ((index = buffer.indexOf('\n')) >= 0) {
      let line = buffer.slice(0, index)
      buffer = buffer.slice(index + 1)
      if (line.endsWith('\r')) line = line.slice(0, -1)
      if (line === '') {
        dispatch()
        continue
      }
      if (line.startsWith('event:')) {
        eventName = line.slice(6).trim()
      } else if (line.startsWith('data:')) {
        dataLines.push(line.slice(5).replace(/^ /, ''))
      }
    }
  }
  dispatch()
}

function onBodyScroll() {
  const el = bodyRef.value
  if (!el) return
  atBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < 60
}

/**
 * 只有用户本来就在底部时才自动跟随；向上翻看历史时不会被流式输出拽回底部
 */
function scrollToBottom(force = false) {
  nextTick(() => {
    const el = bodyRef.value
    if (!el) return
    const nearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 60
    if (force || atBottom.value || nearBottom) {
      el.scrollTop = el.scrollHeight
      atBottom.value = true
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
  position: relative;
  width: 360px;
  height: 520px;
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
  display: flex;
  align-items: center;
  gap: 6px;
}

.cw-badge {
  font-style: normal;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.25);
}

.cw-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cw-clear {
  font-size: 12px;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.18);
  transition: background 0.2s;
}

.cw-clear:hover {
  background: rgba(255, 255, 255, 0.3);
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
  color: #64748b;
  font-size: 13px;
  margin-top: 22px;
  line-height: 1.7;
}

.cw-welcome {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 6px;
}

.cw-sub {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 14px;
}

.cw-quick-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.cw-quick-btn {
  background: #ffffff;
  border: 1px solid #cbd5e1;
  color: #1e293b;
  font-size: 12px;
  padding: 6px 11px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
}

.cw-quick-btn:hover {
  border-color: #3b82f6;
  color: #2563eb;
  background: #eff6ff;
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

.cw-stack {
  max-width: 86%;
}

.cw-bubble {
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
  margin-left: auto;
  width: fit-content;
  max-width: 100%;
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

.cw-refs {
  margin-top: 6px;
  border-left: 3px solid #409eff;
  background: #f0f7ff;
  border-radius: 6px;
  padding: 8px 10px;
}

.cw-refs-title {
  font-size: 11px;
  color: #409eff;
  margin-bottom: 6px;
  font-weight: 600;
}

.cw-ref + .cw-ref {
  margin-top: 6px;
  border-top: 1px dashed #d9ecff;
  padding-top: 6px;
}

.cw-ref-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
}

.cw-ref-doc {
  color: #303133;
  font-weight: 600;
}

.cw-ref-score {
  color: #67c23a;
}

.cw-ref-snippet {
  margin-top: 3px;
  font-size: 11px;
  color: #606266;
  line-height: 1.5;
  white-space: pre-wrap;
}

.cw-scroll-down {
  position: absolute;
  right: 14px;
  bottom: 64px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #d9ecff;
  background: #fff;
  color: #409eff;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
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
