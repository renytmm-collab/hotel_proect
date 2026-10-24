package com.example.hotel_back.Pojo.DAO;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class OccupancyStatsQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private LocalDate startDate;

    private LocalDate endDate;
    
    // 分组方式
    private String groupBy;
}