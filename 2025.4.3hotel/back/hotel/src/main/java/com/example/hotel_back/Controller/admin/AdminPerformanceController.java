package com.example.hotel_back.Controller.admin;

import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.PerformanceAddDTO;
import com.example.hotel_back.Pojo.DAO.PerformanceQueryDTO;
import com.example.hotel_back.Pojo.DAO.PerformanceUpdateDTO;
import com.example.hotel_back.Service.PerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/performance")
@Slf4j
public class AdminPerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody PerformanceAddDTO performanceAddDTO) {
        Long adminId = BaseContext.getCurrentId();
        log.info("添加绩效评估，管理员ID：{}，员工ID：{}", adminId, performanceAddDTO.getEmployeeId());
        
        performanceService.add(adminId, performanceAddDTO);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<PageResult> list(PerformanceQueryDTO queryDTO) {
        log.info("查询绩效评估列表，查询条件：{}", queryDTO);

        PageResult pageResult = performanceService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }
    @PutMapping("/update")
    public Result<Void> update(@RequestBody PerformanceUpdateDTO updateDTO) {
        Long adminId =BaseContext.getCurrentId();
        log.info("修改绩效评估，管理员ID：{}，绩效ID：{}", adminId, updateDTO.getId());

        performanceService.update(adminId, updateDTO);
        return Result.success();
    }
}