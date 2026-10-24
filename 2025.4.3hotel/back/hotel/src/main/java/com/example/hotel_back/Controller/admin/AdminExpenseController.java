package com.example.hotel_back.Controller.admin;

import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.ExpenseAddDTO;
import com.example.hotel_back.Pojo.DAO.ExpenseQueryDTO;
import com.example.hotel_back.Pojo.DAO.ExpenseUpdateDTO;
import com.example.hotel_back.Service.ExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/expense")
@Slf4j
public class AdminExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/list")
    public Result<PageResult> list(ExpenseQueryDTO queryDTO) {
        log.info("查询支出记录列表，查询条件：{}", queryDTO);
        
        PageResult pageResult = expenseService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }
    @PostMapping("/add")
    public Result<Void> add(@RequestBody ExpenseAddDTO expenseAddDTO) {
        // 从JWT中获取当前登录管理员ID
        Long adminId = BaseContext.getCurrentId();
        log.info("添加支出记录，管理员ID：{}，金额：{}", adminId, expenseAddDTO.getAmount());

        expenseService.add(adminId, expenseAddDTO);
        return Result.success();
    }
    @PutMapping("/update")
    public Result<Void> update(@RequestBody ExpenseUpdateDTO expenseUpdateDTO) {
        Long adminId = BaseContext.getCurrentId();
        log.info("修改支出记录，管理员ID：{}，支出ID：{}", adminId, expenseUpdateDTO.getId());

        expenseService.update(adminId, expenseUpdateDTO);
        return Result.success();
    }
}