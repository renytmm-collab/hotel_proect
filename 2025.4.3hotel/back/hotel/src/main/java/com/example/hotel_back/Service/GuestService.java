package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.employeedao.GuestStayRecordQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.ReviewQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.ReviewReplyDTO;

public interface GuestService {
    
    /**
     * 查询客人入住记录
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult getGuestStayRecords(GuestStayRecordQueryDTO queryDTO);
    /**
     * 回复客人评论
     * @param reviewReplyDTO 回复信息
     */
    void replyReview(ReviewReplyDTO reviewReplyDTO);
    /**
     * 获取评价列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult getReviewList(ReviewQueryDTO queryDTO);
}