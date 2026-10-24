package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.PerformanceAddDTO;
import com.example.hotel_back.Pojo.DAO.PerformanceQueryDTO;
import com.example.hotel_back.Pojo.DAO.PerformanceUpdateDTO;

public interface PerformanceService {
    
    /**
     * 添加绩效评估
     * @param adminId 管理员ID
     * @param performanceAddDTO 绩效信息
     */
    void add(Long adminId, PerformanceAddDTO performanceAddDTO);
    /**
     * 分页查询绩效评估
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(PerformanceQueryDTO queryDTO);
    /**
     * 修改绩效评估
     * @param adminId 管理员ID
     * @param updateDTO 更新信息
     */
    void update(Long adminId, PerformanceUpdateDTO updateDTO);
}