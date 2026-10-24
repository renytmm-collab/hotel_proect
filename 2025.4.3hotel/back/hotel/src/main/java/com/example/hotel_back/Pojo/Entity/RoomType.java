package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RoomType implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    
    private String typeName;
    
    private BigDecimal price;
    
    private Integer area;
    
    private String bedType;
    
    private Integer maxGuests;
    
    private String description;
    
    private String facilities;
    
    private Integer weight;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}