package com.example.hotel_back.Controller.employee;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.employeedao.GuestStayRecordQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.ReviewQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.ReviewReplyDTO;
import com.example.hotel_back.Service.GuestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/reception/guest")
@Slf4j
public class ReceptionGuestController {

    @Autowired
    private GuestService guestService;

    @GetMapping("/stayRecords")
    public Result<PageResult> getGuestStayRecords(GuestStayRecordQueryDTO queryDTO) {
        log.info("查询客人入住记录，身份证号：{}", queryDTO.getGuestIdCard());
        
        if (queryDTO.getGuestIdCard() == null || queryDTO.getGuestIdCard().isEmpty()) {
            return Result.error("身份证号不能为空");
        }
        
        PageResult pageResult = guestService.getGuestStayRecords(queryDTO);
        return Result.success(pageResult);
    }
    @PutMapping("/reply")
    public Result<Void> replyReview(@RequestBody ReviewReplyDTO reviewReplyDTO) {
        log.info("回复客人评论，评价ID：{}", reviewReplyDTO.getId());

        guestService.replyReview(reviewReplyDTO);
        return Result.success();
    }
    @GetMapping("/reply/list")
    public Result<PageResult> getReviewList(ReviewQueryDTO queryDTO) {
        log.info("获取评价列表，查询条件：{}", queryDTO);

        PageResult pageResult = guestService.getReviewList(queryDTO);
        return Result.success(pageResult);
    }
}