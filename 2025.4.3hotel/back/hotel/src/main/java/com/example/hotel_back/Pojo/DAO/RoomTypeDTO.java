package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RoomTypeDTO implements Serializable {
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


}