package com.example.hotel_back.mapper;

import com.example.hotel_back.Pojo.VO.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {

    /**
     * 获取客房销售统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 客房销售统计列表
     */
    List<RoomSalesStatVO> getRoomSalesStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取指定房型的房间数量
     * @param roomTypeId 房型ID
     * @return 房间数量
     */
    int getRoomCountByType(@Param("roomTypeId") Long roomTypeId);

    /**
     * 获取指定房型在指定时间段内的实际入住天数
     * @param roomTypeId 房型ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 实际入住天数
     */
    int getActualOccupancyDays(@Param("roomTypeId") Long roomTypeId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);
    /**
     * 按天统计收入
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收入统计列表
     */
    List<IncomeStatVO> getIncomeStatsByDay(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按月统计收入
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收入统计列表
     */
    List<IncomeStatVO> getIncomeStatsByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按年统计收入
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收入统计列表
     */
    List<IncomeStatVO> getIncomeStatsByYear(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按月统计支出
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支出统计列表
     */
    List<ExpenseStatVO> getExpenseStatsByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按年统计支出
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支出统计列表
     */
    List<ExpenseStatVO> getExpenseStatsByYear(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    /**
     * 按月统计利润
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 利润统计列表
     */
    List<ProfitStatVO> getProfitStatsByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按年统计利润
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 利润统计列表
     */
    List<ProfitStatVO> getProfitStatsByYear(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    /**
     * 获取酒店总房间数
     * @return 总房间数
     */
    int getTotalRoomCount();

    /**
     * 按天统计入住率
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 入住率统计列表
     */
    List<OccupancyStatVO> getOccupancyStatsByDay(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    // ===== 数据大屏总览相关 =====

    @Select("SELECT IFNULL(SUM(amount),0) FROM income_record WHERE DATE(payment_time) = CURDATE()")
    BigDecimal getTodayIncome();

    @Select("SELECT COUNT(*) FROM booking_order WHERE DATE(create_time) = CURDATE()")
    Integer getTodayOrderCount();

    @Select("SELECT COUNT(*) FROM room WHERE status = 2")
    Integer getStayingCount();

    @Select("SELECT status AS name, COUNT(*) AS value FROM room GROUP BY status ORDER BY status")
    List<Map<String, Object>> getRoomStatusCount();

    @Select("SELECT payment_method AS name, IFNULL(SUM(amount),0) AS value FROM income_record WHERE payment_method IS NOT NULL AND payment_method != '' GROUP BY payment_method")
    List<Map<String, Object>> getPaymentStats();

    @Select("SELECT IFNULL(SUM(amount),0) FROM income_record")
    BigDecimal getTotalIncome();
}