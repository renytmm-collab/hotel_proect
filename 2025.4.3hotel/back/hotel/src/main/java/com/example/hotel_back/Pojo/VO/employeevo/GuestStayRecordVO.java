package com.example.hotel_back.Pojo.VO.employeevo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GuestStayRecordVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 入住记录ID
    private Long id;
    
    // 订单ID
    private Long orderId;
    
    // 订单号
    private String orderNo;
    
    // 房间号
    private String roomNumber;
    
    // 房型名称
    private String roomTypeName;
    
    // 入住时间
    private LocalDateTime checkInTime;
    
    // 退房时间
    private LocalDateTime checkOutTime;
    
    // 入住天数
    private Integer days;
    
    // 总金额
    private BigDecimal totalAmount;
}