package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RoomTypeImage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 图片ID
    private Long id;
    
    // 房型ID
    private Long roomTypeId;
    
    // 图片URL
    private String imageUrl;
    
    // 是否主图（0否1是）
    private Integer isMain=0;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}