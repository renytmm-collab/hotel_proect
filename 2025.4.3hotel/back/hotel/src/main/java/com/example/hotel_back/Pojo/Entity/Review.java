package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Review implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 评价ID
    private Long id;
    
    // 用户ID
    private Long userId;
    
    // 订单ID
    private Long orderId;
    
    // 房型ID
    private Long roomTypeId;
    
    // 评价内容
    private String content;
    
    // 评分（1-5）
    private Integer score;
    
    // 是否匿名（0否，1是）
    private Integer isAnonymous;
    
    // 状态（0隐藏，1显示）
    private Integer status;
    
    // 回复内容
    private String reply;
    
    // 回复时间
    private LocalDateTime replyTime;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}