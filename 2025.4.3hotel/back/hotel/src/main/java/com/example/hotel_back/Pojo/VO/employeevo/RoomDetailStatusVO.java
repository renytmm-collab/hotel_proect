package com.example.hotel_back.Pojo.VO.employeevo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RoomDetailStatusVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房间ID
    private Long id;
    
    // 房间号
    private String roomNumber;
    
    // 房型ID
    private Long roomTypeId;
    
    // 房型名称
    private String roomTypeName;
    
    // 楼层
    private String floor;
    
    // 房间状态
    private Integer status;
    
    // 入住人姓名
    private String guestName;
    
    // 入住人电话
    private String guestPhone;
    
    // 入住人身份证
    private String guestIdCard;
    
    // 入住时间
    private LocalDateTime checkInTime;
    
    // 退房日期
    private LocalDateTime checkOutDate;
}