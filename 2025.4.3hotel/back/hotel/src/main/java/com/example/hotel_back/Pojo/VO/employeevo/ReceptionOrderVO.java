package com.example.hotel_back.Pojo.VO.employeevo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReceptionOrderVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单ID
    private Long id;
    
    // 订单号
    private String orderNo;
    
    // 用户ID
    private Long userId;
    
    // 房型ID
    private Long roomTypeId;
    
    // 房型名称
    private String roomTypeName;
    
    // 入住日期
    private LocalDate checkInDate;
    
    // 退房日期
    private LocalDate checkOutDate;
    
    // 入住天数
    private Integer days;
    
    // 入住人姓名
    private String guestName;
    
    // 入住人电话
    private String guestPhone;
    
    // 总金额
    private BigDecimal totalAmount;
    
    // 订单状态
    private Integer status;
    
    // 创建时间
    private LocalDateTime createTime;
}