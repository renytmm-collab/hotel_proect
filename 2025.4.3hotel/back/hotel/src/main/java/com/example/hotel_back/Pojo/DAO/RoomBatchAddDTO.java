package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomBatchAddDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房型ID
    private Long roomTypeId;
    
    // 楼层
    private String floor;
    
    // 起始房间号
    private String startNumber;
    
    // 添加数量
    private Integer count;
}