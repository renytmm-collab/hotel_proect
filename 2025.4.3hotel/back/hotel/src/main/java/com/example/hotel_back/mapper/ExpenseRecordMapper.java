package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.ExpenseQueryDTO;
import com.example.hotel_back.Pojo.Entity.ExpenseRecord;
import com.example.hotel_back.Pojo.VO.ExpenseRecordVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ExpenseRecordMapper {
    
    /**
     * 添加支出记录
     * @param expenseRecord 支出记录
     */
    @AutoFill(value = OperationTypeConstant.INSERT)
    @Insert("INSERT INTO expense_record (id, amount, type, payment_method, operator_id, remark, create_time, update_time) " +
            "VALUES (expense_record_seq.nextval, #{amount}, #{type}, #{paymentMethod}, #{operatorId}, #{remark}, SYSDATE, SYSDATE)")
    void insert(ExpenseRecord expenseRecord);
    /**
     * 分页查询支出记录
     * @param queryDTO 查询条件
     * @return 支出记录列表
     */
    Page<ExpenseRecordVO> pageQuery(ExpenseQueryDTO queryDTO);
    /**
     * 根据ID查询支出记录
     * @param id 支出记录ID
     * @return 支出记录
     */
    ExpenseRecord getById(Long id);

    /**
     *
     * @param expenseRecord
     */
    void update(ExpenseRecord expenseRecord);
}