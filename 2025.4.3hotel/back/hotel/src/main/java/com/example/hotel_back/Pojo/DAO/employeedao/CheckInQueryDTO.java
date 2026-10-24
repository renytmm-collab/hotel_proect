package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class CheckInQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房间号
    private String roomNumber;
    
    // 入住人姓名
    private String guestName;
    
    // 入住人电话
    private String phone;
    
    // 入住状态
    private Integer status;
    
    // 入住起始日期
    private LocalDate startDate;

    // 入住终止日期
    private LocalDate endDate;
//    private String startDate;
//    private String endDate;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}