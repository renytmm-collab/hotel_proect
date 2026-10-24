package com.example.hotel_back.Pojo.DAO;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RoomSalesQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    

    private LocalDate startDate;

    private LocalDate endDate;
}