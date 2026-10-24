package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckOutQueryDTO;
import com.example.hotel_back.Pojo.Entity.CheckOutSettlement;
import com.example.hotel_back.Pojo.VO.employeevo.CheckOutListVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface CheckOutSettlementMapper {
    
    /**
     * 添加退房结算记录
     * @param settlement 结算记录
     */
    @Insert("INSERT INTO check_out_settlement (check_in_id, order_id, room_id, user_id, " +
            "extra_fee, damage_fee, total_fee, deposit_refund, payment_method, " +
            "operator_id, remark, create_time, update_time) " +
            "VALUES (#{checkInId}, #{orderId}, #{roomId}, #{userId}, " +
            "#{extraFee}, #{damageFee}, #{totalFee}, #{depositRefund}, #{paymentMethod}, " +
            "#{operatorId}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @AutoFill(value= OperationTypeConstant.INSERT)
    void insert(CheckOutSettlement settlement);
    /**
     * 分页查询结算记录
     * @param checkOutQueryDTO 查询条件
     * @return 结算记录列表
     */
    Page<CheckOutListVO> pageQuery(CheckOutQueryDTO checkOutQueryDTO);
    /**
     * 获取结算详情
     * @param id 结算记录ID
     * @return 结算详情
     */
    CheckOutListVO getDetail(Long id);
}