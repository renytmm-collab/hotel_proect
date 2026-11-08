package com.example.hotel_back.Pojo.VO.employeevo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckInResultVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 入住记录ID
    private Long checkInId;
    
    // 房间号
    private String roomNumber;
}