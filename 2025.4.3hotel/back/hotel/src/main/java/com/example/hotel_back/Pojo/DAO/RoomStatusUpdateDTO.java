package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomStatusUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房间ID
    private Long id;
    
    // 房间状态
    private Integer status;
}