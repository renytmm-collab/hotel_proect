package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderCancelDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单ID
    private Long orderId;
    
    // 取消原因
    private String cancelReason;
}