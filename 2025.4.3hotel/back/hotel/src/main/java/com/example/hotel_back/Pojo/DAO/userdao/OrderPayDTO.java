package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderPayDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单ID
    private Long orderId;
    
    // 支付方式
    private String paymentMethod;
}