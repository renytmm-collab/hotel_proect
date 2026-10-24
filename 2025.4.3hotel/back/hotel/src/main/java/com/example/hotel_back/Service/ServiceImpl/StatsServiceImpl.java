package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Pojo.DAO.*;
import com.example.hotel_back.Pojo.VO.*;
import com.example.hotel_back.Service.StatsService;
import com.example.hotel_back.mapper.StatsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {

    @Autowired
    private StatsMapper statsMapper;

    public List<RoomSalesStatVO> getRoomSalesStats(RoomSalesQueryDTO queryDTO) {
        // 计算日期范围内的总天数
        long totalDays = ChronoUnit.DAYS.between(queryDTO.getStartDate(), queryDTO.getEndDate()) + 1;
        
        // 查询销售统计数据
        List<RoomSalesStatVO> statsList = statsMapper.getRoomSalesStats(
            queryDTO.getStartDate(), 
            queryDTO.getEndDate()
        );
        
        // 计算每种房型的入住率
        for (RoomSalesStatVO stat : statsList) {
            // 查询该房型的房间总数
            int roomCount = statsMapper.getRoomCountByType(stat.getRoomTypeId());
            
            // 计算理论上的最大可入住天数（房间数 * 总天数）
            int maxOccupancyDays = roomCount * (int)totalDays;
            
            // 查询实际入住天数
            int actualOccupancyDays = statsMapper.getActualOccupancyDays(
                stat.getRoomTypeId(), 
                queryDTO.getStartDate(), 
                queryDTO.getEndDate()
            );
            
            // 计算入住率（实际入住天数 / 最大可入住天数 * 100%）
            double occupancyRate = 0.0;
            if (maxOccupancyDays > 0) {
                occupancyRate = (double) actualOccupancyDays / maxOccupancyDays * 100;
            }
            
            // 设置入住率（保留一位小数）
            stat.setOccupancyRate(new java.math.BigDecimal(occupancyRate).setScale(1, java.math.RoundingMode.HALF_UP));
        }
        
        return statsList;
    }
    public List<IncomeStatVO> getIncomeStats(IncomeStatsQueryDTO queryDTO) {
        // 根据分组方式选择不同的SQL查询
        List<IncomeStatVO> statsList;

        switch (queryDTO.getGroupBy()) {
            case "day":
                statsList = statsMapper.getIncomeStatsByDay(
                        queryDTO.getStartDate(),
                        queryDTO.getEndDate()
                );
                break;
            case "month":
                statsList = statsMapper.getIncomeStatsByMonth(
                        queryDTO.getStartDate(),
                        queryDTO.getEndDate()
                );
                break;
            case "year":
                statsList = statsMapper.getIncomeStatsByYear(
                        queryDTO.getStartDate(),
                        queryDTO.getEndDate()
                );
                break;
            default:
                throw new BusinessException("不支持的分组方式");
        }

        // 计算每条记录的总收入
        for (IncomeStatVO stat : statsList) {
            BigDecimal totalAmount = BigDecimal.ZERO;

            // 房费收入
            if (stat.getRoomFee() != null) {
                totalAmount = totalAmount.add(stat.getRoomFee());
            } else {
                stat.setRoomFee(BigDecimal.ZERO);
            }

            // 押金收入
            if (stat.getDepositFee() != null) {
                totalAmount = totalAmount.add(stat.getDepositFee());
            } else {
                stat.setDepositFee(BigDecimal.ZERO);
            }

            // 其他收入
            if (stat.getOtherFee() != null) {
                totalAmount = totalAmount.add(stat.getOtherFee());
            } else {
                stat.setOtherFee(BigDecimal.ZERO);
            }

            // 设置总收入
            stat.setTotalAmount(totalAmount);
        }

        return statsList;
    }
    public List<ExpenseStatVO> getExpenseStats(ExpenseStatsQueryDTO queryDTO) {
        // 根据分组方式选择不同的SQL查询
        List<ExpenseStatVO> statsList;

        switch (queryDTO.getGroupBy()) {
            case "month":
                statsList = statsMapper.getExpenseStatsByMonth(
                        queryDTO.getStartDate(),
                        queryDTO.getEndDate()
                );
                break;
            case "year":
                statsList = statsMapper.getExpenseStatsByYear(
                        queryDTO.getStartDate(),
                        queryDTO.getEndDate()
                );
                break;
            default:
                throw new BusinessException("不支持的分组方式");
        }

        // 计算每条记录的总支出
        for (ExpenseStatVO stat : statsList) {
            BigDecimal totalAmount = BigDecimal.ZERO;

            // 退款支出
            if (stat.getRefundFee() != null) {
                totalAmount = totalAmount.add(stat.getRefundFee());
            } else {
                stat.setRefundFee(BigDecimal.ZERO);
            }

            // 采购支出
            if (stat.getPurchaseFee() != null) {
                totalAmount = totalAmount.add(stat.getPurchaseFee());
            } else {
                stat.setPurchaseFee(BigDecimal.ZERO);
            }

            // 工资支出
            if (stat.getSalaryFee() != null) {
                totalAmount = totalAmount.add(stat.getSalaryFee());
            } else {
                stat.setSalaryFee(BigDecimal.ZERO);
            }

            // 水电费支出
            if (stat.getUtilityFee() != null) {
                totalAmount = totalAmount.add(stat.getUtilityFee());
            } else {
                stat.setUtilityFee(BigDecimal.ZERO);
            }

            // 其他支出
            if (stat.getOtherFee() != null) {
                totalAmount = totalAmount.add(stat.getOtherFee());
            } else {
                stat.setOtherFee(BigDecimal.ZERO);
            }

            // 设置总支出
            stat.setTotalAmount(totalAmount);
        }

        return statsList;
    }
    public List<ProfitStatVO> getProfitStats(ProfitStatsQueryDTO queryDTO) {
        // 根据分组方式选择不同的SQL查询
        List<ProfitStatVO> statsList;

        if ("month".equals(queryDTO.getGroupBy())) {
            statsList = statsMapper.getProfitStatsByMonth(
                    queryDTO.getStartDate(),
                    queryDTO.getEndDate()
            );
        } else if ("year".equals(queryDTO.getGroupBy())) {
            statsList = statsMapper.getProfitStatsByYear(
                    queryDTO.getStartDate(),
                    queryDTO.getEndDate()
            );
        } else {
            throw new BusinessException("不支持的分组方式");
        }

        // 计算每条记录的利润和利润率
        for (ProfitStatVO stat : statsList) {
            // 确保收入和支出不为null
            if (stat.getIncome() == null) {
                stat.setIncome(BigDecimal.ZERO);
            }

            if (stat.getExpense() == null) {
                stat.setExpense(BigDecimal.ZERO);
            }

            // 计算利润
            BigDecimal profit = stat.getIncome().subtract(stat.getExpense());
            stat.setProfit(profit);

            // 计算利润率（利润/收入*100%）
            BigDecimal profitRate = BigDecimal.ZERO;
            if (stat.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                profitRate = profit.divide(stat.getIncome(), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(1, BigDecimal.ROUND_HALF_UP);
            }
            stat.setProfitRate(profitRate);
        }

        return statsList;
    }
    public List<OccupancyStatVO> getOccupancyStats(OccupancyStatsQueryDTO queryDTO) {
        // 获取酒店总房间数
        int totalRooms = statsMapper.getTotalRoomCount();

        // 获取每天的入住房间数
        List<OccupancyStatVO> statsList = statsMapper.getOccupancyStatsByDay(
                queryDTO.getStartDate(),
                queryDTO.getEndDate()
        );

        // 计算每天的入住率
        for (OccupancyStatVO stat : statsList) {
            // 设置总房间数
            stat.setTotalRooms(totalRooms);

            // 计算入住率（已入住房间数 / 总房间数 * 100%）
            BigDecimal occupancyRate = BigDecimal.ZERO;
            if (totalRooms > 0) {
                occupancyRate = new BigDecimal(stat.getOccupiedRooms())
                        .divide(new BigDecimal(totalRooms), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(1, BigDecimal.ROUND_HALF_UP);
            }
            stat.setOccupancyRate(occupancyRate);
        }

        return statsList;
    }
}