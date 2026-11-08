package com.example.hotel_back.Pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleNameVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 角色ID
    private Long id;
    
    // 角色名称
    private String roleName;
}