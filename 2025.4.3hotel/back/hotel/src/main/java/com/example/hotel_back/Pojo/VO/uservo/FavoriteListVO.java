package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FavoriteListVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 收藏ID
    private Long id;
    
    // 房型ID
    private Long roomTypeId;
    
    // 房型名称
    private String typeName;
    
    // 房型价格
    private BigDecimal price;
    
    // 床型
    private String bedType;
    
    // 主图
    private String mainImage;
}