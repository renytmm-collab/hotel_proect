package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 评价ID
    private Long id;
    
    // 评价内容
    private String content;
    
    // 评分
    private Integer score;
    
    // 创建时间
    private LocalDateTime createTime;

    //用户id
    private Long userId;
    
    // 用户头像
    private String userAvatar;
    
    // 评价图片列表
    private List<String> reviewImages;
    
    // 回复内容
    private String reply;
    
    // 回复时间
    private LocalDateTime replyTime;
    
    // 用户名称
    private String username;
    
    // 是否匿名
    private Integer isAnonymous;
}