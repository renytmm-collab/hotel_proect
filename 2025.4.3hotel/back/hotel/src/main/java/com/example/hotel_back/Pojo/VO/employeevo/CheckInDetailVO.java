package com.example.hotel_back.Pojo.VO.employeevo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CheckInDetailVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 入住记录ID
    private Long id;
    
    // 订单ID
    private Long orderId;
    
    // 订单号
    private String orderNo;
    
    // 房间ID
    private Long roomId;
    
    // 房间号
    private String roomNumber;
    
    // 房型名称
    private String roomTypeName;
    
    // 用户ID
    private Long userId;
    
    // 用户昵称
    private String username;
    
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
    
    // 押金
    private BigDecimal deposit;
    
    // 状态
    private Integer status;
    
    // 操作员工ID
    private Long operatorId;
    
    // 操作员工姓名
    private String operatorName;
    
    // 备注
    private String remark;
    
    // 创建时间
    private LocalDateTime createTime;
}