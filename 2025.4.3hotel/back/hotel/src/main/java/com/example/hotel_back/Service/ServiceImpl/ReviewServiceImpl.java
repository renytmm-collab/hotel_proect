package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.constant.OrderStatusConstant;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.userdao.ReviewSubmitDTO;
import com.example.hotel_back.Pojo.Entity.BookingOrder;
import com.example.hotel_back.Pojo.Entity.Review;
import com.example.hotel_back.Pojo.Entity.ReviewImage;
import com.example.hotel_back.Pojo.Entity.RoomType;
import com.example.hotel_back.Pojo.VO.uservo.ReviewListVO;
import com.example.hotel_back.Service.ReviewService;
import com.example.hotel_back.Utils.OrderUtil;
import com.example.hotel_back.mapper.OrderMapper;
import com.example.hotel_back.mapper.ReviewImageMapper;
import com.example.hotel_back.mapper.ReviewMapper;
import com.example.hotel_back.mapper.RoomTypeMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;




@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;
    
    @Autowired
    private ReviewImageMapper reviewImageMapper;
    
    @Autowired
    private OrderMapper orderMapper;

    @Transactional
    public void submitReview(Long userId, ReviewSubmitDTO reviewSubmitDTO) {
        // 查询订单
        BookingOrder order = orderMapper.getById(reviewSubmitDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }

        // 检查订单状态
        if (order.getStatus() != OrderStatusConstant.COMPLETED) {
            throw new BusinessException("订单未完成，无法评价");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime orderUpdateTime = order.getUpdateTime();
        if (orderUpdateTime == null) {
            throw new BusinessException("订单更新时间异常");
        }

        LocalDateTime deadlineTime = orderUpdateTime.plusDays(7);
        if (now.isAfter(deadlineTime)) {
            throw new BusinessException("评价期限已过，只能在订单完成后七天内评价");
        }

        // 检查是否已评价
        if (reviewMapper.existsByOrderId(reviewSubmitDTO.getOrderId())) {
            throw new BusinessException("订单已评价，不能重复评价");
        }

        // 创建评价对象
        Review review = new Review();
        review.setUserId(userId);
        review.setOrderId(reviewSubmitDTO.getOrderId());
        review.setRoomTypeId(reviewSubmitDTO.getRoomTypeId());
        review.setContent(reviewSubmitDTO.getContent());
        review.setScore(reviewSubmitDTO.getScore());
        review.setIsAnonymous(reviewSubmitDTO.getIsAnonymous() != null ? reviewSubmitDTO.getIsAnonymous() : 0);
        review.setStatus(1); // 默认显示

        // 保存评价
        reviewMapper.insert(review);
        Long reviewId =reviewMapper.getIdByOrderId(reviewSubmitDTO.getOrderId());
        review.setId(reviewId);

        // 保存评价图片
        if (reviewSubmitDTO.getImages() != null && !reviewSubmitDTO.getImages().isEmpty()) {
            for (String imageUrl : reviewSubmitDTO.getImages()) {
                ReviewImage reviewImage = new ReviewImage();
                reviewImage.setReviewId(review.getId());
                reviewImage.setImageUrl(imageUrl);
                reviewImage.setCreateTime(LocalDateTime.now());

                reviewImageMapper.insert(reviewImage);
            }
        }
        adjustRoomTypeWeight(reviewSubmitDTO.getRoomTypeId(), reviewSubmitDTO.getScore());

        log.info("评价提交成功，评价ID：{}，已调整房型权重", review.getId());
    }
    /**
     * 根据评价分数调整房型权重
     * @param roomTypeId 房型ID
     * @param score 评分
     */
    private void adjustRoomTypeWeight(Long roomTypeId, Integer score) {
        // 获取当前房型信息
        RoomType roomType = roomTypeMapper.getById(roomTypeId);
        if (roomType == null) {
            log.error("房型不存在，无法调整权重，房型ID：{}", roomTypeId);
            return;
        }

        // 获取当前房型的平均评分
        Double avgScore = reviewMapper.getAvgScoreByRoomTypeId(roomTypeId);
        if (avgScore == null) {
            avgScore = (double) score; // 如果没有评分，使用当前评分
        }

        int newWeight = OrderUtil.newWeight(avgScore);

        // 更新房型权重
        roomTypeMapper.updateWeight(roomTypeId, newWeight);

        log.info("房型权重调整成功，房型ID：{}，新权重：{}，平均评分：{}", roomTypeId, newWeight, avgScore);
    }
    public PageResult pageQueryByUserId(Long userId, int pageNum, int pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);

        // 查询评价列表
        Page<ReviewListVO> page = reviewMapper.pageQueryByUserId(userId);

        // 获取评价ID列表
        List<Long> reviewIds = page.getResult().stream()
                .map(ReviewListVO::getId)
                .collect(Collectors.toList());

        // 如果有评价，查询评价图片
        if (!reviewIds.isEmpty()) {
            // 查询评价图片
            Map<Long, List<String>> reviewImagesMap = reviewImageMapper.getImagesByReviewIds(reviewIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            ReviewImage::getReviewId,
                            Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
                    ));

            // 设置评价图片
            for (ReviewListVO reviewVO : page.getResult()) {
                reviewVO.setImages(reviewImagesMap.getOrDefault(reviewVO.getId(), Collections.emptyList()));
            }
        }

        // 封装结果
        return new PageResult(page.getTotal(), page.getResult());
    }
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        // 查询评价
        Review review = reviewMapper.getById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }

        // 检查评价是否属于当前用户
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评价");
        }

        // 删除评价图片
        reviewImageMapper.deleteByReviewId(reviewId);

        // 删除评价
        reviewMapper.deleteById(reviewId);

        log.info("评价删除成功，评价ID：{}", reviewId);
    }
}