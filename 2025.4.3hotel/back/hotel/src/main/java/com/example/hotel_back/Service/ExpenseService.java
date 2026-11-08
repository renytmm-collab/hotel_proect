package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.ExpenseAddDTO;
import com.example.hotel_back.Pojo.DAO.ExpenseQueryDTO;
import com.example.hotel_back.Pojo.DAO.ExpenseUpdateDTO;

public interface ExpenseService {
    
    /**
     * 分页查询支出记录
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(ExpenseQueryDTO queryDTO);
    /**
     * 添加支出记录
     * @param adminId 管理员ID
     * @param expenseAddDTO 支出信息
     */
    void add(Long adminId, ExpenseAddDTO expenseAddDTO);
    /**
     * 修改支出记录
     * @param adminId 管理员ID
     * @param expenseUpdateDTO 支出更新信息
     */
    void update(Long adminId, ExpenseUpdateDTO expenseUpdateDTO);
}