package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RoomTypeQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房型ID
    private Long roomTypeId;
    
    // 最低价格
    private BigDecimal minPrice;
    
    // 最高价格
    private BigDecimal maxPrice;
    
    // 最大入住人数
    private Integer maxGuests;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}