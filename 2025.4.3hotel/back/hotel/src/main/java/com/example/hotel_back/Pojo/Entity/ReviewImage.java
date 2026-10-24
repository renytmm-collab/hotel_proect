package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ReviewImage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 图片ID
    private Long id;
    
    // 评价ID
    private Long reviewId;
    
    // 图片URL
    private String imageUrl;
    
    // 创建时间
    private LocalDateTime createTime;
}