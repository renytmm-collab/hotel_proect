package com.example.hotel_back.Pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeInfoVO implements Serializable {
    private Long id;
    private String empNo;
    private String username="帅气小子";
    private String realName;
    private String phone;
    private String avatar;
    private Long roleId;
    private String roleName;
    private Integer status;
    private LocalDateTime createTime;
    private String sex;
}