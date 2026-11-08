package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Room implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房间ID
    private Long id;
    
    // 房间号
    private String roomNumber;
    
    // 房型ID
    private Long roomTypeId;
    
    // 楼层
    private String floor;
    
    // 状态（0 已预定，1 空闲，2 已入住，3 清洁中，4 维修中,5 下架）
    private Integer status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}