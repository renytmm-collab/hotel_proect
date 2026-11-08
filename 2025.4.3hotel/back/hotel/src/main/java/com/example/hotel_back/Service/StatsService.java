package com.example.hotel_back.Service;

import com.example.hotel_back.Pojo.DAO.*;
import com.example.hotel_back.Pojo.VO.ExpenseStatVO;
import com.example.hotel_back.Pojo.VO.IncomeStatVO;
import com.example.hotel_back.Pojo.VO.ProfitStatVO;
import com.example.hotel_back.Pojo.VO.RoomSalesStatVO;
import com.example.hotel_back.Pojo.VO.OccupancyStatVO;

import java.util.List;

public interface StatsService {
    
    /**
     * 获取客房销售统计
     * @param queryDTO 查询条件
     * @return 客房销售统计列表
     */
    List<RoomSalesStatVO> getRoomSalesStats(RoomSalesQueryDTO queryDTO);
    /**
     * 获取收入统计
     * @param queryDTO 查询条件
     * @return 收入统计列表
     */
    List<IncomeStatVO> getIncomeStats(IncomeStatsQueryDTO queryDTO);
    /**
     * 获取支出统计
     * @param queryDTO 查询条件
     * @return 支出统计列表
     */
    List<ExpenseStatVO> getExpenseStats(ExpenseStatsQueryDTO queryDTO);
    /**
     * 获取利润统计
     * @param queryDTO 查询条件
     * @return 利润统计列表
     */
    List<ProfitStatVO> getProfitStats(ProfitStatsQueryDTO queryDTO);
    /**
     * 获取入住率统计
     * @param queryDTO 查询条件
     * @return 入住率统计列表
     */
    List<OccupancyStatVO> getOccupancyStats(OccupancyStatsQueryDTO queryDTO);
}