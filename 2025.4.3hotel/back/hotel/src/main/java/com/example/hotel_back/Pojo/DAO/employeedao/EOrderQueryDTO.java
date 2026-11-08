package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EOrderQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单号
    private String orderNo;
    
    // 入住人姓名
    private String guestName;
    
    // 入住人电话
    private String guestPhone;
    
    // 入住人身份证
    private String guestIdCard;
    
    // 订单状态
    private Integer status;

    private LocalDate checkInDate;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}