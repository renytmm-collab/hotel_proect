package com.example.hotel_back.Pojo.DAO;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class EmployeePageQueryDTO implements Serializable {
    private String realName;
    private String empNo;
    private Long roleId;
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // 创建开始时间
    private LocalDateTime beginTime;
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // 创建结束时间
    private LocalDateTime endTime;
    private Integer status;
    // 页码
    private int pageNum = 1;

    // 每页显示记录数
    private int pageSize = 10;
    String startDate;
    String endDate;
}