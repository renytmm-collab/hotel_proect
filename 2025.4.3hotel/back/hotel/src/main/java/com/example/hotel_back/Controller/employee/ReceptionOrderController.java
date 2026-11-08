package com.example.hotel_back.Controller.employee;

import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.employeedao.EOrderCancelDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.EOrderQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.OrderUpdateDTO;
import com.example.hotel_back.Pojo.VO.employeevo.ReceptionOrderDetailVO;
import com.example.hotel_back.Service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/reception/order")
@Slf4j
public class ReceptionOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    public Result<PageResult> list(EOrderQueryDTO orderQueryDTO) {
        log.info("查询预订订单，查询条件：{}", orderQueryDTO);
        
        PageResult pageResult = orderService.pageQueryForReception(orderQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/{id}")
    public Result<ReceptionOrderDetailVO> getOrderDetail(@PathVariable Long id) {
        log.info("获取预订订单详情，订单ID：{}", id);

        ReceptionOrderDetailVO orderDetail = orderService.getOrderDetailForReception(id);
        return Result.success(orderDetail);
    }
    @PutMapping("/update")
    public Result<String> updateOrder(@RequestBody OrderUpdateDTO orderUpdateDTO) {
        // 从JWT中获取当前登录员工ID
        Long employeeId = BaseContext.getCurrentId();
        log.info("修改预订信息，员工ID：{}，订单ID：{}", employeeId, orderUpdateDTO.getOrderId());

        orderService.updateOrderByReception(employeeId, orderUpdateDTO);
        return Result.success();
    }
    @PostMapping("/cancel")
    public Result<String> cancelOrder(@RequestBody EOrderCancelDTO orderCancelDTO) {
        Long employeeId =BaseContext.getCurrentId();
        log.info("取消预订，员工ID：{}，订单ID：{}", employeeId, orderCancelDTO.getOrderId());

        orderService.cancelOrderByReception(employeeId, orderCancelDTO);
        return Result.success();
    }
}