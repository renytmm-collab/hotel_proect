package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewListVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 评价ID
    private Long id;
    
    // 评价内容
    private String content;
    
    // 评分
    private Integer score;
    
    // 是否匿名
    private Integer isAnonymous;
    
    // 房型名称
    private String roomTypeName;
    
    // 订单ID
    private Long orderId;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 评价图片列表
    private List<String> images;
    
    // 回复内容
    private String reply;
    
    // 回复时间
    private LocalDateTime replyTime;
}