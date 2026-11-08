package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.employeedao.GuestStayRecordQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.ReviewQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.ReviewReplyDTO;
import com.example.hotel_back.Pojo.Entity.Review;
import com.example.hotel_back.Pojo.VO.employeevo.GuestStayRecordVO;
import com.example.hotel_back.Pojo.VO.uservo.ReviewVO;
import com.example.hotel_back.Service.GuestService;
import com.example.hotel_back.mapper.GuestMapper;
import com.example.hotel_back.mapper.ReviewImageMapper;
import com.example.hotel_back.mapper.ReviewMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class GuestServiceImpl implements GuestService {

    @Autowired
    private GuestMapper guestMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ReviewImageMapper reviewImageMapper;

    public PageResult getGuestStayRecords(GuestStayRecordQueryDTO queryDTO) {
        // 开启分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 执行查询
        Page<GuestStayRecordVO> page = guestMapper.getGuestStayRecords(queryDTO.getGuestIdCard());
        
        // 获取结果
        long total = page.getTotal();
        List<GuestStayRecordVO> records = page.getResult();
        
        // 封装结果
        return new PageResult(total, records);
    }
    public void replyReview(ReviewReplyDTO reviewReplyDTO) {
        // 检查评价是否存在
        Review review = reviewMapper.getById(reviewReplyDTO.getId());
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        review.setReply(reviewReplyDTO.getReply());
        review.setReplyTime(LocalDateTime.now());

        // 更新评价回复
        reviewMapper.updateReply(review);


        log.info("回复客人评论成功，评价ID：{}", reviewReplyDTO.getId());

    }
    public PageResult getReviewList(ReviewQueryDTO queryDTO) {
        // 开启分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 执行查询
        Page<ReviewVO> page = reviewMapper.pageQuery(queryDTO);

        // 获取结果
        long total = page.getTotal();
        List<ReviewVO> records = page.getResult();

        // 查询评价图片
        for (ReviewVO reviewVO : records) {
            List<String> images = reviewImageMapper.getImagesByReviewId(reviewVO.getId());
            reviewVO.setReviewImages(images);
        }

        // 封装结果
        return new PageResult(total, records);
    }
}