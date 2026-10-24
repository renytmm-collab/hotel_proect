package com.example.hotel_back.Pojo.DAO;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ProfitStatsQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 开始日期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    // 结束日期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    // 分组方式（month/year）
    private String groupBy;
}