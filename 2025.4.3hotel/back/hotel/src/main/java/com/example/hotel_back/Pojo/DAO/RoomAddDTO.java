package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomAddDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String roomNumber;

    private Long roomTypeId;

    private String floor;
}