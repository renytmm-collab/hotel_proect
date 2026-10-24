package com.example.hotel_back.Pojo.VO.employeevo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatusVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 签到状态
    // -1：未签到未签退（初始状态）
    // 0：正常（已签到已签退）
    // 1：迟到（已签到未签退）
    // 2：早退（已签到已签退）
    // 3：旷工
    // 4：请假（未签到未签退）
    // 5：已签到未签退
    private Integer status;
}