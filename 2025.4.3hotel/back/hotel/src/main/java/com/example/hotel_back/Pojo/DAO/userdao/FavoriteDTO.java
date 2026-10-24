package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;

@Data
public class FavoriteDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房型ID
    private Long roomTypeId;
}