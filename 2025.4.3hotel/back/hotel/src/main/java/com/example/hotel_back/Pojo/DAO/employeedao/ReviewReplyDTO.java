package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewReplyDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 评价ID
    private Long id;
    
    // 回复内容
    private String reply;
}