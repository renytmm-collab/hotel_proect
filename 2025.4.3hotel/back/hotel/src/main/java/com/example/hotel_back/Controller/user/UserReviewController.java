package com.example.hotel_back.Controller.user;

import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.userdao.ReviewSubmitDTO;
import com.example.hotel_back.Service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/review")
@Slf4j
public class UserReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody ReviewSubmitDTO reviewSubmitDTO) {
        // 从JWT中获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("提交评价，用户ID：{}，评价信息：{}", userId, reviewSubmitDTO);
        
//        // 参数校验
//        if (reviewSubmitDTO.getOrderId() == null) {
//            return Result.error("订单ID不能为空");
//        }
//        if (reviewSubmitDTO.getRoomTypeId() == null) {
//            return Result.error("房型ID不能为空");
//        }
//        if (reviewSubmitDTO.getContent() == null || reviewSubmitDTO.getContent().isEmpty()) {
//            return Result.error("评价内容不能为空");
//        }
//        if (reviewSubmitDTO.getScore() == null || reviewSubmitDTO.getScore() < 1 || reviewSubmitDTO.getScore() > 5) {
//            return Result.error("评分必须在1-5之间");
//        }
        
        reviewService.submitReview(userId, reviewSubmitDTO);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<PageResult> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId =BaseContext.getCurrentId();
        log.info("获取我的评价列表，用户ID：{}，页码：{}，每页记录数：{}", userId, pageNum, pageSize);

        PageResult pageResult = reviewService.pageQueryByUserId(userId, pageNum, pageSize);
        return Result.success(pageResult);
    }
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除评价，用户ID：{}，评价ID：{}", userId, id);

        reviewService.deleteReview(userId, id);
        return Result.success();
    }
}