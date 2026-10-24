package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingOrder implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单ID
    private Long id;
    
    // 订单号
    private String orderNo;
    
    // 用户ID
    private Long userId;
    
    // 房型ID
    private Long roomTypeId;
    
    // 房间单价
    private BigDecimal roomPrice;
    
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
    
    // 入住人身份证
    private String guestIdCard;
    
    // 总金额
    private BigDecimal totalAmount;
    
    // 订单状态（0已取消，1待支付，2已支付待入住，3已入住，4已完成）
    private Integer status;
    
    // 支付方式
    private String paymentMethod;
    
    // 支付时间
    private LocalDateTime paymentTime;
    
    // 支付单号
    private String paymentNo;
    
    // 取消原因
    private String cancelReason;
    
    // 取消时间
    private LocalDateTime cancelTime;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}