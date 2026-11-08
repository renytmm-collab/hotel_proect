package com.example.hotel_back.Controller.admin;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.IncomeQueryDTO;
import com.example.hotel_back.Service.IncomeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/income")
@Slf4j
public class AdminIncomeController {

    @Autowired
    private IncomeService incomeService;

    @GetMapping("/list")
    public Result<PageResult> list(IncomeQueryDTO queryDTO) {
        log.info("查询收入记录列表，查询条件：{}", queryDTO);
        
        PageResult pageResult = incomeService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }
}