package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RoleVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private Long id;
    private String roleName;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
}