package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class HotRoomTypeVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房型ID
    private Long id;
    
    // 房型名称
    private String typeName;
    
    // 价格
    private BigDecimal price;
    
    // 面积
    private Integer area;
    
    // 床型
    private String bedType;
    
    // 最大入住人数
    private Integer maxGuests;
    
    // 描述
    private String description;
    
    // 主图
    private String mainImage;
}