package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;

import java.io.Serializable;

@Data
public class GuestStayRecordQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 客人身份证号
    private String guestIdCard;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}