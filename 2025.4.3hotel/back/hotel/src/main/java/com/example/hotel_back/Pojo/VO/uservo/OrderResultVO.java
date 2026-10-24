package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderResultVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单ID
    private Long orderId;
    
    // 订单号
    private String orderNo;
    
    // 总金额
    private BigDecimal totalAmount;
    
    // 订单状态
    private Integer status;
}