package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomPageQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房间号
    private String roomNumber;
    
    // 房型ID
    private Long roomTypeId;
    
    // 楼层
    private String floor;
    
    // 状态
    private Integer status;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}