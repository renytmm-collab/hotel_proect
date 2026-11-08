package com.example.hotel_back.Controller.admin;

import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.*;
import com.example.hotel_back.Pojo.VO.*;
import com.example.hotel_back.Service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/stats")
@Slf4j
public class AdminStatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/roomSales")
    public Result<List<RoomSalesStatVO>> getRoomSalesStats(RoomSalesQueryDTO queryDTO) {
        log.info("查询客房销售统计，开始日期：{}，结束日期：{}", queryDTO.getStartDate(), queryDTO.getEndDate());
        
        // 参数校验
        if (queryDTO.getStartDate() == null || queryDTO.getEndDate() == null) {
            return Result.error("开始日期和结束日期不能为空");
        }
        
        if (queryDTO.getStartDate().isAfter(queryDTO.getEndDate())) {
            return Result.error("开始日期不能晚于结束日期");
        }
        
        List<RoomSalesStatVO> statsList = statsService.getRoomSalesStats(queryDTO);
        return Result.success(statsList);
    }
    @GetMapping("/income")
    public Result<List<IncomeStatVO>> getIncomeStats(IncomeStatsQueryDTO queryDTO) {
        log.info("查询收入统计，开始日期：{}，结束日期：{}，分组方式：{}",
                queryDTO.getStartDate(), queryDTO.getEndDate(), queryDTO.getGroupBy());

        // 参数校验
        if (queryDTO.getStartDate() == null || queryDTO.getEndDate() == null) {
            return Result.error("开始日期和结束日期不能为空");
        }

        if (queryDTO.getStartDate().isAfter(queryDTO.getEndDate())) {
            return Result.error("开始日期不能晚于结束日期");
        }

        if (queryDTO.getGroupBy() == null ||
                (!queryDTO.getGroupBy().equals("day") &&
                        !queryDTO.getGroupBy().equals("month") &&
                        !queryDTO.getGroupBy().equals("year"))) {
            return Result.error("分组方式必须为day、month或year");
        }

        List<IncomeStatVO> statsList = statsService.getIncomeStats(queryDTO);
        return Result.success(statsList);
    }
    @GetMapping("/expense")
    public Result<List<ExpenseStatVO>> getExpenseStats(ExpenseStatsQueryDTO queryDTO) {
        log.info("查询支出统计，开始日期：{}，结束日期：{}，分组方式：{}",
                queryDTO.getStartDate(), queryDTO.getEndDate(), queryDTO.getGroupBy());

        // 参数校验
        if (queryDTO.getStartDate() == null || queryDTO.getEndDate() == null) {
            return Result.error("开始日期和结束日期不能为空");
        }

        if (queryDTO.getStartDate().isAfter(queryDTO.getEndDate())) {
            return Result.error("开始日期不能晚于结束日期");
        }

        if (queryDTO.getGroupBy() == null ||
                (!queryDTO.getGroupBy().equals("month") &&
                        !queryDTO.getGroupBy().equals("year"))) {
            return Result.error("分组方式必须为month或year");
        }

        List<ExpenseStatVO> statsList = statsService.getExpenseStats(queryDTO);
        return Result.success(statsList);
    }
    @GetMapping("/profit")
    public Result<List<ProfitStatVO>> getProfitStats(ProfitStatsQueryDTO queryDTO) {
        log.info("查询利润统计，开始日期：{}，结束日期：{}，分组方式：{}",
                queryDTO.getStartDate(), queryDTO.getEndDate(), queryDTO.getGroupBy());

        // 参数校验
        if (queryDTO.getStartDate() == null || queryDTO.getEndDate() == null) {
            return Result.error("开始日期和结束日期不能为空");
        }

        if (queryDTO.getStartDate().isAfter(queryDTO.getEndDate())) {
            return Result.error("开始日期不能晚于结束日期");
        }

        if (queryDTO.getGroupBy() == null ||
                (!queryDTO.getGroupBy().equals("month") &&
                        !queryDTO.getGroupBy().equals("year"))) {
            return Result.error("分组方式必须为month或year");
        }

        List<ProfitStatVO> statsList = statsService.getProfitStats(queryDTO);
        return Result.success(statsList);
    }
    @GetMapping("/occupancy")
    public Result<List<OccupancyStatVO>> getOccupancyStats(OccupancyStatsQueryDTO queryDTO) {
        log.info("查询入住率统计，开始日期：{}，结束日期：{}，分组方式：{}",
                queryDTO.getStartDate(), queryDTO.getEndDate(), queryDTO.getGroupBy());


        if (queryDTO.getStartDate() == null || queryDTO.getEndDate() == null) {
            return Result.error("开始日期和结束日期不能为空");
        }

        if (queryDTO.getStartDate().isAfter(queryDTO.getEndDate())) {
            return Result.error("开始日期不能晚于结束日期");
        }

        // 目前只支持按天统计
        if (queryDTO.getGroupBy() == null || !queryDTO.getGroupBy().equals("day")) {
            queryDTO.setGroupBy("day");
        }

        List<OccupancyStatVO> statsList = statsService.getOccupancyStats(queryDTO);
        return Result.success(statsList);
    }
}