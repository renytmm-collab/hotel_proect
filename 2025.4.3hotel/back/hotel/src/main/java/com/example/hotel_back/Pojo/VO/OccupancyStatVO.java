package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OccupancyStatVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 日期
    private String date;
    
    // 总房间数
    private Integer totalRooms;
    
    // 已入住房间数
    private Integer occupiedRooms;
    
    // 入住率
    private BigDecimal occupancyRate;
}