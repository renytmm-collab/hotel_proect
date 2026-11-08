package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.userdao.ReviewSubmitDTO;

public interface ReviewService {
    /**
     * 提交评价
     * @param userId 用户ID
     * @param reviewSubmitDTO 评价信息
     */
    void submitReview(Long userId, ReviewSubmitDTO reviewSubmitDTO);
    /**
     * 分页查询用户评价
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @return 分页结果
     */
    PageResult pageQueryByUserId(Long userId, int pageNum, int pageSize);
    /**
     * 删除评价
     * @param userId 用户ID
     * @param reviewId 评价ID
     */
    void deleteReview(Long userId, Long reviewId);
}