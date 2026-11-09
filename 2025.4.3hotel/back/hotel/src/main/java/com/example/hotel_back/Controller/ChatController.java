package com.example.hotel_back.Controller;

import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 智能客服机器人接口（大模型）
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 发送客服消息
     * @param body 请求体，格式：{ "messages": [ { "role": "user", "content": "..." }, ... ] }
     * @return 回复内容
     */
    @PostMapping("/send")
    public Result<String> send(@RequestBody Map<String, Object> body) {
        Object messagesObj = body.get("messages");
        if (!(messagesObj instanceof List)) {
            return Result.error("参数错误");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) messagesObj;
        if (messages.isEmpty()) {
            return Result.error("消息不能为空");
        }

        log.info("收到客服消息，共 {} 条", messages.size());
        String reply = chatService.chat(messages);
        return Result.success(reply);
    }
}
