package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderListVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单ID
    private Long id;
    
    // 订单号
    private String orderNo;
    
    // 房型名称
    private String roomTypeName;
    
    // 房间单价
    private BigDecimal roomPrice;
    
    // 入住日期
    private LocalDate checkInDate;
    
    // 退房日期
    private LocalDate checkOutDate;
    
    // 入住天数
    private Integer days;
    
    // 总金额
    private BigDecimal totalAmount;
    
    // 订单状态
    private Integer status;
    
    // 创建时间
    private LocalDateTime createTime;
}