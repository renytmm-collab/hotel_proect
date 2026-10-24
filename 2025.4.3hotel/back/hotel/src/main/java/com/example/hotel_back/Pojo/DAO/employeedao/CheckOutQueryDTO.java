package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class CheckOutQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单号
    private String orderNo;
    
    // 入住人姓名
    private String guestName;
    
    // 开始日期
    private LocalDate startDate;

    // 结束日期
    private LocalDate endDate;
//private String startDate;
//    private String endDate;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}