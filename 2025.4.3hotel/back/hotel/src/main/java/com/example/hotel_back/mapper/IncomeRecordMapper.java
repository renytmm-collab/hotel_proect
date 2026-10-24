package com.example.hotel_back.mapper;

import com.example.hotel_back.Pojo.DAO.IncomeQueryDTO;
import com.example.hotel_back.Pojo.Entity.IncomeRecord;
import com.example.hotel_back.Pojo.VO.IncomeRecordVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface IncomeRecordMapper {
    
    /**
     * 添加收入记录
     * @param incomeRecord 收入记录
     */
    @Insert("INSERT INTO income_record (order_id, amount, type, payment_method, payment_time, operator_id, remark, create_time) " +
            "VALUES (#{orderId}, #{amount}, #{type}, #{paymentMethod},NOW(), #{operatorId}, #{remark}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(IncomeRecord incomeRecord);
    /**
     * 分页查询收入记录
     * @param queryDTO 查询条件
     * @return 收入记录列表
     */
    Page<IncomeRecordVO> pageQuery(IncomeQueryDTO queryDTO);
}