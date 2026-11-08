package com.example.hotel_back.Controller.employee;

import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckOutDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckOutQueryDTO;
import com.example.hotel_back.Pojo.VO.employeevo.CheckOutListVO;
import com.example.hotel_back.Service.CheckOutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/employee/reception/checkOut")
@Slf4j
public class ReceptionCheckOutController {

    @Autowired
    private CheckOutService checkOutService;

    @PostMapping("/handle")
    public Result<Void> handleCheckOut(@RequestBody CheckOutDTO checkOutDTO) {
        Long employeeId = BaseContext.getCurrentId();
        log.info("办理退房，员工ID：{}，入住记录ID：{}", employeeId, checkOutDTO.getCheckInId());
        
        checkOutService.handleCheckOut(employeeId, checkOutDTO);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<PageResult> list(CheckOutQueryDTO checkOutQueryDTO) {
        log.info("查询结算记录，查询条件：{}", checkOutQueryDTO);

        PageResult pageResult = checkOutService.pageQuery(checkOutQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/{id}")
    public Result<CheckOutListVO> getDetail(@PathVariable Long id) {
        log.info("查询结算详情，ID：{}", id);

        CheckOutListVO detail = checkOutService.getDetail(id);
        return Result.success(detail);
    }
}