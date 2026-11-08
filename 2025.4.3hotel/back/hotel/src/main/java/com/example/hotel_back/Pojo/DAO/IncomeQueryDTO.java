package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class IncomeQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 收入类型
    private Integer type;
    
    // 开始日期
    private LocalDate startDate;
    
    // 结束日期
    private LocalDate endDate;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}