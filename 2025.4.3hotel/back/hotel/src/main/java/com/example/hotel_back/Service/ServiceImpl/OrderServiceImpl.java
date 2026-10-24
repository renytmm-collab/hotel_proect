package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.constant.ExpenseTypeConstant;
import com.example.hotel_back.Common.constant.ImcomeTypeConstant;
import com.example.hotel_back.Common.constant.OrderStatusConstant;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.employeedao.EOrderCancelDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.EOrderQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.OrderUpdateDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderCancelDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderCreateDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderPayDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderQueryDTO;
import com.example.hotel_back.Pojo.Entity.ExpenseRecord;
import com.example.hotel_back.Pojo.Entity.IncomeRecord;
import com.example.hotel_back.Pojo.VO.employeevo.ReceptionOrderDetailVO;
import com.example.hotel_back.Pojo.VO.employeevo.ReceptionOrderVO;
import com.example.hotel_back.Pojo.VO.uservo.OrderDetailVO;
import com.example.hotel_back.Pojo.VO.uservo.OrderListVO;
import com.example.hotel_back.Pojo.Entity.BookingOrder;
import com.example.hotel_back.Pojo.Entity.RoomType;
import com.example.hotel_back.Pojo.VO.uservo.OrderResultVO;
import com.example.hotel_back.Service.OrderService;
import com.example.hotel_back.Utils.OrderUtil;
import com.example.hotel_back.mapper.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private RoomTypeMapper roomTypeMapper;
    
    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private IncomeRecordMapper incomeRecordMapper;
    @Autowired
    private ExpenseRecordMapper expenseRecordMapper;
    @Transactional
    public OrderResultVO createOrder(Long userId, OrderCreateDTO orderCreateDTO) {


        // 查询房型信息
        RoomType roomType = roomTypeMapper.getById(orderCreateDTO.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }


        // 计算入住天数
        long days = ChronoUnit.DAYS.between(orderCreateDTO.getCheckInDate(), orderCreateDTO.getCheckOutDate());

        // 检查该时间段是否有可用房间
        // 1. 获取该房型的总房间数
        int totalRooms = roomMapper.countRoomsByRoomTypeId(orderCreateDTO.getRoomTypeId());

        // 2. 获取该时间段内已预订的房间数
        // 注意：这里需要检查所有状态为待支付、已支付待入住、已入住的订单
        // 并且订单的入住和退房日期与当前预订时间有重叠
        int bookedRooms = orderMapper.countBookedRooms(
                orderCreateDTO.getRoomTypeId(),
                orderCreateDTO.getCheckInDate(),
                orderCreateDTO.getCheckOutDate()
        );

        // 3. 判断是否还有可用房间
        if (totalRooms <= bookedRooms) {
            throw new BusinessException("该时间段房间已满，请选择其他日期或房型");
        }

        // 生成订单号
        String orderNo = OrderUtil.generateOrderNo();

        // 计算总金额
        BigDecimal totalAmount = roomType.getPrice().multiply(new BigDecimal(days));

        // 创建订单对象
        BookingOrder order = new BookingOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setRoomTypeId(orderCreateDTO.getRoomTypeId());
        order.setRoomPrice(roomType.getPrice());
        order.setCheckInDate(orderCreateDTO.getCheckInDate());
        order.setCheckOutDate(orderCreateDTO.getCheckOutDate());
        order.setDays((int) days);
        order.setGuestName(orderCreateDTO.getGuestName());
        order.setGuestPhone(orderCreateDTO.getGuestPhone());
        order.setGuestIdCard(orderCreateDTO.getGuestIdCard());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatusConstant.PENDING_PAYMENT); // 待支付

        // 保存订单
        orderMapper.insert(order);


        // 构建返回结果
        OrderResultVO orderResultVO = new OrderResultVO();
        orderResultVO.setOrderId(orderMapper.getByOrderNo(orderNo).getId());
        orderResultVO.setOrderNo(order.getOrderNo());
        orderResultVO.setTotalAmount(order.getTotalAmount());
        orderResultVO.setStatus(order.getStatus());

        return orderResultVO;
    }
    @Transactional
    public void payOrder(Long userId, OrderPayDTO orderPayDTO) {
        // 查询订单
        BookingOrder order = orderMapper.getById(orderPayDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }

        // 检查订单状态
        if (order.getStatus() != OrderStatusConstant.PENDING_PAYMENT) {
            throw new BusinessException("订单状态不正确，无法支付");
        }

        // 生成支付单号
        String paymentNo = OrderUtil.generatePaymentNo();

        // 更新订单状态
        order.setStatus(OrderStatusConstant.PENDING_CHECK_IN); // 已支付待入住
        order.setPaymentMethod(orderPayDTO.getPaymentMethod());
        order.setPaymentTime(LocalDateTime.now());
        order.setPaymentNo(paymentNo);

        // 保存订单
        orderMapper.updateAfterPayment(order);

        log.info("订单支付成功，订单ID：{}，支付方式：{}，支付单号：{}", order.getId(), order.getPaymentMethod(), order.getPaymentNo());
        // 添加收入记录
        IncomeRecord incomeRecord = new IncomeRecord();
        incomeRecord.setOrderId(order.getId());
        incomeRecord.setAmount(order.getTotalAmount());
        incomeRecord.setType(ImcomeTypeConstant.ROOM_FEE); // 房费
        incomeRecord.setPaymentMethod(order.getPaymentMethod());
        incomeRecord.setPaymentTime(order.getPaymentTime());
        incomeRecord.setOperatorId(0L); // 在线支付没有操作员
        incomeRecord.setRemark("预订订单在线支付");
        incomeRecord.setCreateTime(LocalDateTime.now());

        // 保存收入记录
        incomeRecordMapper.insert(incomeRecord);

        log.info("订单支付成功，订单ID：{}，支付方式：{}，支付单号：{}，已添加收入记录",
                order.getId(), order.getPaymentMethod(), order.getPaymentNo());
    }
    @Transactional
    public void cancelOrder(Long userId, OrderCancelDTO orderCancelDTO) {
        // 查询订单
        BookingOrder order = orderMapper.getById(orderCancelDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() !=OrderStatusConstant.PENDING_PAYMENT)
            throw new BusinessException("请联系客服");

        // 更新订单状态
        order.setStatus(OrderStatusConstant.CANCELED); // 已取消
        order.setCancelReason(orderCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());

        // 保存订单
        orderMapper.updateAfterCancel(order);

        log.info("订单取消成功，订单ID：{}，取消原因：{}", order.getId(), order.getCancelReason());
    }
    public PageResult pageQuery(OrderQueryDTO orderQueryDTO) {
        // 开启分页
        PageHelper.startPage(orderQueryDTO.getPageNum(), orderQueryDTO.getPageSize());

        // 执行查询
        Page<OrderListVO> page = orderMapper.pageQuery(orderQueryDTO);

        // 获取结果
        long total = page.getTotal();
        List<OrderListVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    public OrderDetailVO getOrderDetail(Long userId, Long orderId) {
        // 查询订单详情
        OrderDetailVO orderDetailVO = orderMapper.getOrderDetail(orderId);
        if (orderDetailVO == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单是否属于当前用户
        if (!userId.equals(orderMapper.getUserIdByOrderId(orderId))) {
            throw new BusinessException("无权查看此订单");
        }


        return orderDetailVO;
    }
    public PageResult pageQueryForReception(EOrderQueryDTO orderQueryDTO) {
        // 开启分页
        PageHelper.startPage(orderQueryDTO.getPageNum(), orderQueryDTO.getPageSize());

        // 执行查询
        Page<ReceptionOrderVO> page = orderMapper.pageQueryForReception(orderQueryDTO);

        // 获取结果
        long total = page.getTotal();
        List<ReceptionOrderVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }

    public ReceptionOrderDetailVO getOrderDetailForReception(Long id) {
        // 查询订单详情
        ReceptionOrderDetailVO orderDetail = orderMapper.getOrderDetailForReception(id);

        if (orderDetail == null) {
            throw new BusinessException("订单不存在");
        }

        return orderDetail;
    }


    @Transactional
    public void updateOrderByReception(Long employeeId, OrderUpdateDTO orderUpdateDTO) {
        // 查询订单
        BookingOrder order = orderMapper.getById(orderUpdateDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单状态，只有已支付待入住的订单才能修改
        if (order.getStatus() != OrderStatusConstant.PENDING_CHECK_IN) {
            throw new BusinessException("只有已支付待入住的订单才能修改");
        }

        // 保存原始总金额，用于后续比较
        BigDecimal originalTotalAmount = order.getTotalAmount();
        Long roomtypeid=order.getRoomTypeId();

        // 如果修改了房型，需要查询新房型信息
        RoomType roomType = null;
        if (orderUpdateDTO.getRoomTypeId()!= null ) {
            // 判断orderUpdateDTO.getRoomTypeId()是否为null且与order.getRoomTypeId()不相等
            if (!order.getRoomTypeId().equals(orderUpdateDTO.getRoomTypeId())) {
                roomType = roomTypeMapper.getById(orderUpdateDTO.getRoomTypeId());
                roomtypeid=orderUpdateDTO.getRoomTypeId();
                if (roomType == null) {
                    throw new BusinessException("房型不存在");
                }
                // 这里可以根据实际需求对roomType进行进一步处理或使用
            }
        }

        // 计算入住天数
        long days = ChronoUnit.DAYS.between(orderUpdateDTO.getCheckInDate(), orderUpdateDTO.getCheckOutDate());
        if (days <= 0) {
            throw new BusinessException("退房日期必须晚于入住日期");
        }

        // 检查该时间段是否有可用房间（排除当前订单）
        // 1. 获取该房型的总房间数

        int totalRooms = roomMapper.countRoomsByRoomTypeId(roomtypeid);

        // 2. 获取该时间段内已预订的房间数（排除当前订单）
        int bookedRooms = orderMapper.countBookedRoomsExcludeOrder(
                roomtypeid,
                orderUpdateDTO.getCheckInDate(),
                orderUpdateDTO.getCheckOutDate(),
                orderUpdateDTO.getOrderId()
        );

        // 3. 判断是否还有可用房间
        if (totalRooms <= bookedRooms) {
            throw new BusinessException("该时间段房间已满，请选择其他日期或房型");
        }

        // 计算新的总金额
        BigDecimal roomPrice = roomType != null ? roomType.getPrice() : order.getRoomPrice();
        BigDecimal totalAmount = roomPrice.multiply(new BigDecimal(days));

        // 更新订单信息
        BookingOrder updatedOrder = new BookingOrder();
        updatedOrder.setId(orderUpdateDTO.getOrderId());
        updatedOrder.setRoomTypeId(orderUpdateDTO.getRoomTypeId());
        updatedOrder.setRoomPrice(roomPrice);
        updatedOrder.setCheckInDate(orderUpdateDTO.getCheckInDate());
        updatedOrder.setCheckOutDate(orderUpdateDTO.getCheckOutDate());
        updatedOrder.setDays((int) days);
        updatedOrder.setGuestName(orderUpdateDTO.getGuestName());
        updatedOrder.setGuestPhone(orderUpdateDTO.getGuestPhone());
        updatedOrder.setGuestIdCard(orderUpdateDTO.getGuestIdCard());
        updatedOrder.setTotalAmount(totalAmount);


        // 比较价格变化
        int priceComparison = totalAmount.compareTo(originalTotalAmount);

        // 如果价格变少了，需要退费并记录支出
        if (priceComparison < 0) {
            // 计算退款金额
            BigDecimal refundAmount = originalTotalAmount.subtract(totalAmount);

            // 创建支出记录
            ExpenseRecord expenseRecord = new ExpenseRecord();
            expenseRecord.setAmount(refundAmount);
            expenseRecord.setType(ExpenseTypeConstant.REFUND); // 退款
            expenseRecord.setPaymentMethod(order.getPaymentMethod()); // 使用原订单的支付方式
            expenseRecord.setOperatorId(employeeId);
            expenseRecord.setRemark("订单修改退款，订单号：" + order.getOrderNo());
            expenseRecord.setCreateTime(LocalDateTime.now());
            expenseRecord.setUpdateTime(LocalDateTime.now());

            // 保存支出记录
            expenseRecordMapper.insert(expenseRecord);

            log.info("订单修改退款，订单ID：{}，退款金额：{}", order.getId(), refundAmount);
        }
        // 如果价格变多了
        else if (priceComparison > 0) {
            BigDecimal incomeAmount = totalAmount.subtract(originalTotalAmount);
            IncomeRecord incomeRecord = new IncomeRecord();
            incomeRecord.setAmount(incomeAmount);
            incomeRecord.setType(ImcomeTypeConstant.ROOM_FEE);
            incomeRecord.setPaymentMethod(order.getPaymentMethod());
            incomeRecord.setOperatorId(employeeId);
            incomeRecord.setOrderId(order.getId());
            incomeRecord.setRemark("订单修改收入，订单号：" + order.getOrderNo());
            incomeRecordMapper.insert(incomeRecord);


        }

        // 保存订单
        orderMapper.updateByReception(updatedOrder);

        // 记录操作日志
        log.info("前台接待修改预订信息成功，员工ID：{}，订单ID：{}", employeeId, orderUpdateDTO.getOrderId());
    }
    @Transactional
    public void cancelOrderByReception(Long employeeId, EOrderCancelDTO orderCancelDTO) {
        // 查询订单
        BookingOrder order = orderMapper.getById(orderCancelDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单状态，只有已支付待入住的订单才能取消
        if (order.getStatus() != OrderStatusConstant.PENDING_CHECK_IN) {
            throw new BusinessException("只有已支付待入住的订单才能取消");
        }

        // 更新订单状态
        BookingOrder updatedOrder = new BookingOrder();
        updatedOrder.setId(orderCancelDTO.getOrderId());
        updatedOrder.setStatus(OrderStatusConstant.CANCELED); // 已取消
        updatedOrder.setCancelReason(orderCancelDTO.getCancelReason());
        updatedOrder.setCancelTime(LocalDateTime.now());

        // 保存订单
        orderMapper.updateCancelStatus(updatedOrder);

        // 创建退款记录
        ExpenseRecord expenseRecord = new ExpenseRecord();
        expenseRecord.setAmount(order.getTotalAmount());
        expenseRecord.setType(ExpenseTypeConstant.REFUND); // 退款
        expenseRecord.setPaymentMethod(order.getPaymentMethod()); // 使用原订单的支付方式
        expenseRecord.setOperatorId(employeeId);
        expenseRecord.setRemark("订单取消退款，订单号：" + order.getOrderNo() + "，原因：" + orderCancelDTO.getCancelReason());
        expenseRecord.setCreateTime(LocalDateTime.now());
        expenseRecord.setUpdateTime(LocalDateTime.now());

        // 保存退款记录
        expenseRecordMapper.insert(expenseRecord);

        log.info("前台接待取消预订成功，员工ID：{}，订单ID：{}，退款金额：{}",
                employeeId, orderCancelDTO.getOrderId(), order.getTotalAmount());
    }
}