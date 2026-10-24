package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RoomSalesStatVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房型ID
    private Long roomTypeId;
    
    // 房型名称
    private String roomTypeName;
    
    // 订单数量
    private Integer orderCount;
    
    // 总金额
    private BigDecimal totalAmount;
    
    // 入住率
    private BigDecimal occupancyRate;
}