package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Favorite implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 收藏ID
    private Long id;
    
    // 用户ID
    private Long userId;
    
    // 房型ID
    private Long roomTypeId;
    
    // 创建时间
    private LocalDateTime createTime;
}