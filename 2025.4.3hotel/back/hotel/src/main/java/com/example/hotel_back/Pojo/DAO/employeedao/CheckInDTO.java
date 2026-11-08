package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CheckInDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单ID
    private Long orderId;
    
    // 房间ID
    private Long roomId;

    // 用户ID
    private Long userId;
    
    // 入住人姓名
    private String guestName;
    
    // 入住人电话
    private String guestPhone;
    
    // 入住人身份证
    private String guestIdCard;
    

    
    // 备注
    private String remark;
    private BigDecimal deposit;
}