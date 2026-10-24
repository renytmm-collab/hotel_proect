package com.example.hotel_back.Controller.user;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.userdao.OrderCancelDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderCreateDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderPayDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderQueryDTO;
import com.example.hotel_back.Pojo.VO.uservo.OrderDetailVO;
import com.example.hotel_back.Pojo.VO.uservo.OrderResultVO;
import com.example.hotel_back.Service.OrderService;
import com.example.hotel_back.Common.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<OrderResultVO> create(@RequestBody OrderCreateDTO orderCreateDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("创建预订订单，用户ID：{}，订单信息：{}", userId, orderCreateDTO);
        
//        // 参数校验
//        if (orderCreateDTO.getRoomTypeId() == null) {
//            return Result.error("房型ID不能为空");
//        }
//        if (orderCreateDTO.getCheckInDate() == null) {
//            return Result.error("入住日期不能为空");
//        }
//        if (orderCreateDTO.getCheckOutDate() == null) {
//            return Result.error("退房日期不能为空");
//        }
//        if (orderCreateDTO.getGuestName() == null || orderCreateDTO.getGuestName().isEmpty()) {
//            return Result.error("入住人姓名不能为空");
//        }
//        if (orderCreateDTO.getGuestPhone() == null || orderCreateDTO.getGuestPhone().isEmpty()) {
//            return Result.error("入住人电话不能为空");
//        }
//        if (orderCreateDTO.getGuestIdCard() == null || orderCreateDTO.getGuestIdCard().isEmpty()) {
//            return Result.error("入住人身份证不能为空");
//        }
        
//        // 日期校验
//        LocalDate today = LocalDate.now();
//        if (orderCreateDTO.getCheckInDate().isBefore(today)) {
//            return Result.error("入住日期不能早于今天");
//        }
//        if (orderCreateDTO.getCheckOutDate().isBefore(orderCreateDTO.getCheckInDate())) {
//            return Result.error("退房日期不能早于入住日期");
//        }
        
        // 计算入住天数
        long days = ChronoUnit.DAYS.between(orderCreateDTO.getCheckInDate(), orderCreateDTO.getCheckOutDate());
        if (days <= 0) {
            return Result.error("入住天数必须大于0");
        }
        
        OrderResultVO orderResultVO = orderService.createOrder(userId, orderCreateDTO);
        return Result.success(orderResultVO);
    }
    @PostMapping("/pay")
    public Result<String> pay(@RequestBody OrderPayDTO orderPayDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("支付订单，用户ID：{}，订单ID：{}，支付方式：{}", userId, orderPayDTO.getOrderId(), orderPayDTO.getPaymentMethod());

        // 参数校验
        if (orderPayDTO.getOrderId() == null) {
            return Result.error("订单ID不能为空");
        }
        if (orderPayDTO.getPaymentMethod() == null || orderPayDTO.getPaymentMethod().isEmpty()) {
            return Result.error("支付方式不能为空");
        }

        orderService.payOrder(userId, orderPayDTO);
        return Result.success();
    }
    @PostMapping("/cancel")
    public Result<String> cancel(@RequestBody OrderCancelDTO orderCancelDTO) {
        Long userId =BaseContext.getCurrentId();
        log.info("取消订单，用户ID：{}，订单ID：{}，取消原因：{}", userId, orderCancelDTO.getOrderId(), orderCancelDTO.getCancelReason());

        // 参数校验
        if (orderCancelDTO.getOrderId() == null) {
            return Result.error("订单ID不能为空");
        }

        orderService.cancelOrder(userId, orderCancelDTO);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<PageResult> list(OrderQueryDTO orderQueryDTO) {
        // 从JWT中获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("获取订单列表，用户ID：{}，查询条件：{}", userId, orderQueryDTO);

        // 设置用户ID
        orderQueryDTO.setUserId(userId);

        PageResult pageResult = orderService.pageQuery(orderQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/{id}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取订单详情，用户ID：{}，订单ID：{}", userId, id);

        OrderDetailVO orderDetailVO = orderService.getOrderDetail(userId, id);
        return Result.success(orderDetailVO);
    }
}