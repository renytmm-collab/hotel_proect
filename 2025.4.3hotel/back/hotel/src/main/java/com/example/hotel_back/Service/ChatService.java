package com.example.hotel_back.Service;

import java.util.List;
import java.util.Map;

public interface ChatService {

    /**
     * 调用大模型进行客服问答
     * @param messages 对话历史，每项包含 role 和 content
     * @return 客服回复文本
     */
    String chat(List<Map<String, Object>> messages);
}
