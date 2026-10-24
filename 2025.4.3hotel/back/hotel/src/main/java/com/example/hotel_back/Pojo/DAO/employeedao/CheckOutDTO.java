package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CheckOutDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 入住记录ID
    private Long checkInId;
    
    // 额外费用
    private BigDecimal extraFee;
    
    // 损坏赔偿费
    private BigDecimal damageFee;
    
    // 押金退还
    private BigDecimal depositRefund;
    
    // 支付方式
    private String paymentMethod;
    
    // 备注
    private String remark;
}