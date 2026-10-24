package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckInQueryDTO;
import com.example.hotel_back.Pojo.Entity.CheckIn;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInDetailVO;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInListVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;


@Mapper
public interface CheckInMapper {
    
    /**
     * 添加入住记录
     * @param checkIn 入住记录
     */
    @Insert("INSERT INTO check_in_record (id, order_id, room_id, user_id, guest_name, guest_phone, guest_id_card, " +
            "check_in_time, check_out_date, deposit, status, remark, operator_id, create_time, update_time) " +
            "VALUES (CHECK_IN_RECORD_SEQ.nextval, #{orderId}, #{roomId}, #{userId}, #{guestName}, #{guestPhone}, #{guestIdCard}, " +
            "#{checkInTime}, #{checkOutDate}, #{deposit}, #{status}, #{remark}, #{operatorId}, SYSDATE, SYSDATE)")
    @AutoFill(value=OperationTypeConstant.INSERT)
    void insert(CheckIn checkIn);
    /**
     * 分页查询入住记录
     * @param checkInQueryDTO 查询条件
     * @return 入住记录列表
     */
    Page<CheckInListVO> pageQuery(CheckInQueryDTO checkInQueryDTO);
    /**
     * 获取入住详情
     * @param id 入住记录ID
     * @return 入住详情
     */
    CheckInDetailVO getDetail(Long id);
    /**
     * 根据ID查询入住记录
     * @param id 入住记录ID
     * @return 入住记录
     */
    CheckIn getById(Long id);

    /**
     * 更新入住记录状态
     * @param id 入住记录ID
     * @param status 状态
     * @param checkOutTime 退房时间
     */

    void updateStatus(Long id, Integer status, LocalDateTime checkOutTime);
}