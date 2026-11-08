package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RoomVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 房间ID
    private Long id;
    
    // 房间号
    private String roomNumber;
    
    // 房型ID
    private Long roomTypeId;
    
    // 房型名称
    private String roomTypeName;
    
    // 楼层
    private String floor;
    
    // 状态
    private Integer status;
    
    // 创建时间
    private LocalDateTime createTime;
}