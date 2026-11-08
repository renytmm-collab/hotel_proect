package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class OrderUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单ID
    private Long orderId;
    
    // 房型ID
    private Long roomTypeId;
    
    // 入住日期
    private LocalDate checkInDate;
    
    // 退房日期
    private LocalDate checkOutDate;
    
    // 入住人姓名
    private String guestName;
    
    // 入住人电话
    private String guestPhone;
    
    // 入住人身份证
    private String guestIdCard;
}