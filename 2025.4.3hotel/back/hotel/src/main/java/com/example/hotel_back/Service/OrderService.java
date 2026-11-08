package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.employeedao.EOrderCancelDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.EOrderQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.OrderUpdateDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderCancelDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderCreateDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderPayDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderQueryDTO;
import com.example.hotel_back.Pojo.VO.employeevo.ReceptionOrderDetailVO;
import com.example.hotel_back.Pojo.VO.uservo.OrderDetailVO;
import com.example.hotel_back.Pojo.VO.uservo.OrderResultVO;

public interface OrderService {


        /**
         * 创建预订订单
         * @param userId 用户ID
         * @param orderCreateDTO 订单信息
         * @return 订单结果
         */
        OrderResultVO createOrder(Long userId, OrderCreateDTO orderCreateDTO);

    /**
     * 支付订单
     * @param userId 用户ID
     * @param orderPayDTO 支付信息
     */
    void payOrder(Long userId, OrderPayDTO orderPayDTO);
    /**
     * 取消订单
     * @param userId 用户ID
     * @param orderCancelDTO 取消信息
     */
    void cancelOrder(Long userId, OrderCancelDTO orderCancelDTO);
    /**
     * 分页查询订单
     * @param orderQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(OrderQueryDTO orderQueryDTO);
    /**
     * 获取订单详情
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(Long userId, Long orderId);
    /**
     * 前台接待分页查询订单
     * @param orderQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQueryForReception(EOrderQueryDTO orderQueryDTO);
    /**
     * 获取前台接待订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    ReceptionOrderDetailVO getOrderDetailForReception(Long id);
    /**
     * 前台接待修改预订信息
     * @param employeeId 员工ID
     * @param orderUpdateDTO 修改信息
     */
    void updateOrderByReception(Long employeeId, OrderUpdateDTO orderUpdateDTO);
    /**
     * 前台接待取消预订
     * @param employeeId 员工ID
     * @param orderCancelDTO 取消信息
     */
    void cancelOrderByReception(Long employeeId, EOrderCancelDTO orderCancelDTO);
}
