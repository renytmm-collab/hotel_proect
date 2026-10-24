package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ReviewSubmitDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 订单ID
    private Long orderId;
    
    // 房型ID
    private Long roomTypeId;
    
    // 评价内容
    private String content;
    
    // 评分（1-5）
    private Integer score;
    
    // 是否匿名（0否，1是）
    private Integer isAnonymous;
    
    // 评价图片URL列表
    private List<String> images;
}