package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CheckIn implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 入住记录ID
    private Long id;
    
    // 订单ID
    private Long orderId;
    
    // 房间ID
    private Long roomId;

    //入住人ID
    private Long userId;
    
    // 入住人姓名
    private String guestName;
    
    // 入住人电话
    private String guestPhone;
    
    // 入住人身份证
    private String guestIdCard;
    
    // 入住时间
    private LocalDateTime checkInTime;
    
    // 预计退房日期
    private LocalDate checkOutDate;
    
    // 实际退房时间
    private LocalDateTime checkOutTime;
    
    // 押金
    private BigDecimal deposit;
    
    // 状态（1：在住，2：已退房）
    private Integer status;
    
    // 备注
    private String remark;
    
    // 操作员工ID
    private Long operatorId;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}