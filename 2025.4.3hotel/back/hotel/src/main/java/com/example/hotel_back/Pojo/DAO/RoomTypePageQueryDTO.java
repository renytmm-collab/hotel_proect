package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomTypePageQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房型名称
    private String typeName;
    
    // 状态
    private Integer status;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}