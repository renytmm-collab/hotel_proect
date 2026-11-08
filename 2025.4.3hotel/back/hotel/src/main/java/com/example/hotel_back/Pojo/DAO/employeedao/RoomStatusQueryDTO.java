package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomStatusQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 楼层
    private String floor;
    
    // 房间状态
    private Integer status;
    
    // 房型ID
    private Long roomTypeId;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}