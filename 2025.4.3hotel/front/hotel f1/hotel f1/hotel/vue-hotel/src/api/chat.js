import request from '@/utils/request'

/**
 * 发送智能客服消息（大模型）
 * @param {Array} messages 对话历史，格式 [{ role: 'user'|'assistant', content: '...' }]
 * @returns {Promise}
 */
export function sendChatMessage(messages) {
  return request({
    url: '/api/chat/send',
    method: 'post',
    data: { messages },
    timeout: 60000 // 大模型响应较慢，覆盖全局 10s 超时
  })
}

export default {
  sendChatMessage
}
