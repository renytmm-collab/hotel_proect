package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomTypeImageVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 图片ID
    private Long id;
    
    // 图片URL
    private String imageUrl;
    
    // 是否主图
    private Integer isMain;
}