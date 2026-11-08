package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.IncomeQueryDTO;

public interface IncomeService {
    
    /**
     * 分页查询收入记录
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(IncomeQueryDTO queryDTO);
}