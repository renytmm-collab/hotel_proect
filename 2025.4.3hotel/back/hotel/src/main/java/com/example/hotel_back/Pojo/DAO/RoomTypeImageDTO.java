package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomTypeImageDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房型ID
    private Long roomTypeId;
    
    // 是否主图（0否1是）
    private Integer isMain;
    
    // 图片路径
    private String imageUrl;
}