package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Service.ChatService;
import com.example.hotel_back.mapper.ChatRagMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Value("${hotel.deepseek.api-key}")
    private String apiKey;

    @Value("${hotel.deepseek.base-url}")
    private String baseUrl;

    @Value("${hotel.deepseek.model}")
    private String model;

    /** RAG 检索层：从数据库实时查询知识 */
    @Autowired
    private ChatRagMapper chatRagMapper;

    /**
     * 系统提示词：只保留「角色设定 + 通用政策 + 行为要求」。
     * 房价、房型、空房、评价等动态知识不再写死在这里，而是在运行时从数据库检索后拼接进来（RAG）。
     */
    private static final String SYSTEM_PROMPT = """
            你是"逸云酒店"的智能客服助手，请协助顾客完成客房预订与咨询。

            通用信息：
            - 入住时间：14:00后，退房时间：次日12:00前
            - 支持在线预订、到店支付

            要求：
            1. 用亲切、礼貌、简洁的中文回答，控制在150字以内。
            2. 房价、房型、空房、评价等事实，必须严格以"实时检索数据"为准，禁止编造数据库里没有的数字。
            3. 若检索数据中没有顾客所需信息，请引导顾客前往"客房预订"页面查看，或建议联系前台。
            4. 超出酒店服务范围的问题，礼貌说明并建议联系前台。
            5. 使用纯文本，不要使用markdown格式。
            """;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String chat(List<Map<String, Object>> messages) {
        // RAG 第 1 步：从用户最新问题中抽取检索意图
        String query = extractLatestUserQuery(messages);

        // RAG 第 2 步：检索（Retrieval）——从数据库实时查询相关知识
        String knowledge = retrieveKnowledge(query);
        if (knowledge != null && !knowledge.isEmpty()) {
            log.info("RAG 检索到知识片段（{} 字），已注入提示词", knowledge.length());
        }

        // RAG 第 3 步：增强（Augmented）——把检索结果拼进系统提示词
        String systemContent = SYSTEM_PROMPT;
        if (knowledge != null && !knowledge.isEmpty()) {
            systemContent = SYSTEM_PROMPT + "\n\n以下是刚刚从数据库实时检索到的知识，请优先依据这些信息回答：\n" + knowledge;
        }

        List<Map<String, Object>> fullMessages = new ArrayList<>();
        Map<String, Object> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", systemContent);
        fullMessages.add(system);
        fullMessages.addAll(messages);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", fullMessages);
        body.put("stream", false);
        body.put("temperature", 0.7);
        body.put("max_tokens", 500);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // RAG 第 4 步：生成（Generation）——大模型结合检索数据回答
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/chat/completions", entity, Map.class);
            Map<?, ?> respBody = resp.getBody();
            if (respBody == null) {
                return "抱歉，我暂时无法回答这个问题。";
            }
            Object choicesObj = respBody.get("choices");
            if (choicesObj instanceof List && !((List<?>) choicesObj).isEmpty()) {
                Object first = ((List<?>) choicesObj).get(0);
                if (first instanceof Map) {
                    Object messageObj = ((Map<?, ?>) first).get("message");
                    if (messageObj instanceof Map) {
                        Object content = ((Map<?, ?>) messageObj).get("content");
                        if (content != null) {
                            return content.toString().trim();
                        }
                    }
                }
            }
            return "抱歉，我暂时无法回答这个问题。";
        } catch (Exception e) {
            log.error("调用大模型失败", e);
            return "抱歉，客服系统暂时不可用，请稍后再试或致电前台。";
        }
    }

    /**
     * 提取对话历史中最新一条用户消息（作为检索查询）。
     */
    private String extractLatestUserQuery(List<Map<String, Object>> messages) {
        if (messages == null) {
            return "";
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            Map<String, Object> m = messages.get(i);
            if (m != null && "user".equals(m.get("role"))) {
                Object content = m.get("content");
                return content == null ? "" : content.toString();
            }
        }
        return "";
    }

    /**
     * 基于关键词的轻量级检索：判断用户意图，命中后查询对应表，拼装成知识片段。
     * （答辩可说明：此处用 MySQL 关键词检索替代向量库，是"轻量 RAG"实现）
     */
    private String retrieveKnowledge(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        // 意图一：房价 / 房型 / 设施
        if (containsAny(query, "房", "价格", "多少钱", "房价", "费用", "大床", "双床", "套房",
                "总统", "亲子", "面积", "床", "设施", "wifi", "WiFi", "WIFI", "免费", "网络",
                "几人间", "住", "介绍", "描述")) {
            List<Map<String, Object>> roomTypes = chatRagMapper.listRoomTypes();
            if (roomTypes != null && !roomTypes.isEmpty()) {
                sb.append("【实时房型数据】\n");
                for (Map<String, Object> rt : roomTypes) {
                    sb.append("- ").append(rt.get("typeName"))
                            .append("：").append(rt.get("price")).append("元/晚，")
                            .append(rt.get("area")).append("㎡，")
                            .append(rt.get("bedType")).append("，可住")
                            .append(rt.get("maxGuests")).append("人；设施：")
                            .append(rt.get("facilities")).append("\n");
                }
            }
        }

        // 意图二：空房 / 预订
        if (containsAny(query, "空房", "有空", "空余", "可订", "预订", "有房", "还有没有房", "房源", "能不能订")) {
            List<Map<String, Object>> freeRooms = chatRagMapper.countFreeRooms();
            if (freeRooms != null && !freeRooms.isEmpty()) {
                sb.append("【当前空房情况】\n");
                for (Map<String, Object> fr : freeRooms) {
                    sb.append("- ").append(fr.get("typeName")).append("：空闲 ")
                            .append(fr.get("freeCount")).append(" 间\n");
                }
            }
        }

        // 意图三：口碑 / 评价
        if (containsAny(query, "评价", "口碑", "好评", "差评", "评分", "怎么样", "体验", "反馈", "环境")) {
            List<Map<String, Object>> reviews = chatRagMapper.listRecentReviews();
            if (reviews != null && !reviews.isEmpty()) {
                sb.append("【近期住客评价】\n");
                for (Map<String, Object> r : reviews) {
                    sb.append("- ").append(r.get("content")).append("（")
                            .append(r.get("score")).append(" 分）\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * 判断文本中是否包含任意一个关键词。
     */
    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
