package com.example.hotel_back.Pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeNameVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房型ID
    private Long id;
    
    // 房型名称
    private String typeName;
}