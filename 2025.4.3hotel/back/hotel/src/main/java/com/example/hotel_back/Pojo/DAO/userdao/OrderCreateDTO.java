package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class OrderCreateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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