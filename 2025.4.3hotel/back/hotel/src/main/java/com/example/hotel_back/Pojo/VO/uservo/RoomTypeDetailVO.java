package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomTypeDetailVO implements Serializable {
    
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
    
    // 设施
    private String facilities;
    
    // 权重
    private Integer weight;
    
    // 状态
    private Integer status;
    
    // 房型图片列表
    private List<RoomTypeImageVO> images;
    
    // 评论列表
    private List<ReviewVO> reviews;
}