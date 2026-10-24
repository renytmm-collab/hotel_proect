package com.example.hotel_back.Pojo.VO.employeevo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomStatusVO implements Serializable {
    
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
}