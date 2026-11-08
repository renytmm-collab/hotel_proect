package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单状态
    private Integer status;
    
    // 用户ID（由系统设置，不由前端传入）
    private Long userId;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}