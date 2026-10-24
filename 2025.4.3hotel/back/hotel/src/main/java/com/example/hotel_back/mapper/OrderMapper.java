package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.employeedao.EOrderQueryDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderQueryDTO;
import com.example.hotel_back.Pojo.Entity.BookingOrder;
import com.example.hotel_back.Pojo.VO.employeevo.ReceptionOrderDetailVO;
import com.example.hotel_back.Pojo.VO.employeevo.ReceptionOrderVO;
import com.example.hotel_back.Pojo.VO.uservo.OrderDetailVO;
import com.example.hotel_back.Pojo.VO.uservo.OrderListVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;

@Mapper
public interface OrderMapper {
    /**
     * 添加订单
     * @param order 订单信息
     */
    @Insert("insert into booking_order (id, order_no, user_id, room_type_id, room_price, check_in_date, check_out_date, " +
            "days, guest_name, guest_phone, guest_id_card, total_amount, status, create_time, update_time) " +
            "values (booking_order_seq.nextval, #{orderNo}, #{userId}, #{roomTypeId}, #{roomPrice}, #{checkInDate}, #{checkOutDate}, " +
            "#{days}, #{guestName}, #{guestPhone}, #{guestIdCard}, #{totalAmount}, #{status}, SYSDATE, SYSDATE)")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insert(BookingOrder order);
    /**
     * 根据订单号查询订单
     * @param order_id 订单号
     */
    @Select("select * from booking_order where order_no = #{order_id}")
    BookingOrder getByOrderNo(String order_id);
    
    /**
     * 统计指定时间段内已预订的房间数量
     * @param roomTypeId 房型ID
     * @param checkInDate 入住日期
     * @param checkOutDate 退房日期
     * @return 已预订的房间数量
     */
    @Select("select count(*) from booking_order " +
            "where room_type_id = #{roomTypeId} " +
            "and status in (1, 2, 3) " + // 待支付、已支付待入住、已入住
            "and not (TRUNC(check_out_date) <= TRUNC(#{checkInDate, jdbcType=DATE}) or TRUNC(check_in_date) >= TRUNC(#{checkOutDate, jdbcType=DATE}))")
    int countBookedRooms(@Param("roomTypeId") Long roomTypeId,
                         @Param("checkInDate") LocalDate checkInDate,
                         @Param("checkOutDate") LocalDate checkOutDate);
    /**
     * 根据ID查询订单
     * @param id 订单ID
     * @return 订单信息
     */
    @Select("select * from booking_order where id = #{id}")
    BookingOrder getById(Long id);

    /**
     * 支付后更新订单
     * @param order 订单信息
     */
    @Update("update booking_order set status = #{status}, payment_method = #{paymentMethod}, " +
            "payment_time =SYSDATE, payment_no = #{paymentNo}, update_time = SYSDATE " +
            "where id = #{id}")
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updateAfterPayment(BookingOrder order);
    /**
     * 取消后更新订单
     * @param order 订单信息
     */
    @Update("update booking_order set status = #{status}, cancel_reason = #{cancelReason}, " +
            "cancel_time = SYSDATE, update_time = SYSDATE " +
            "where id = #{id}")
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updateAfterCancel(BookingOrder order);
    /**
     * 分页查询订单
     * @param orderQueryDTO 查询条件
     * @return 订单列表
     */
    Page<OrderListVO> pageQuery(OrderQueryDTO orderQueryDTO);
    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(Long orderId);

    /**
     * 根据订单ID获取用户ID
     * @param orderId 订单ID
     * @return 用户ID
     */
    @Select("select user_id from booking_order where id = #{orderId}")
    Long getUserIdByOrderId(Long orderId);
    /**
     * 前台接待分页查询订单
     * @param orderQueryDTO 查询条件
     * @return 订单列表
     */
    Page<ReceptionOrderVO> pageQueryForReception(EOrderQueryDTO orderQueryDTO);
    /**
     * 获取前台接待订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    ReceptionOrderDetailVO getOrderDetailForReception(Long id);
    /**
     * 获取该时间段内已预订的房间数（排除指定订单）
     * @param roomTypeId 房型ID
     * @param checkInDate 入住日期
     * @param checkOutDate 退房日期
     * @param orderId 要排除的订单ID
     * @return 已预订房间数
     */
    int countBookedRoomsExcludeOrder(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, Long orderId);

    /**
     * 前台接待修改订单信息
     * @param order 订单信息
     */
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updateByReception(BookingOrder order);
    /**
     * 更新订单取消状态
     * @param order 订单信息
     */
@AutoFill(value = OperationTypeConstant.UPDATE)
    void updateCancelStatus(BookingOrder order);
    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);
}