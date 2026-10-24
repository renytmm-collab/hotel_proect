package com.example.hotel_back.Controller.employee;

import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckInDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckInQueryDTO;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInDetailVO;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInResultVO;
import com.example.hotel_back.Service.CheckInService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/reception")
@Slf4j
public class ReceptionCheckInController {

    @Autowired
    private CheckInService checkInService;

    @PostMapping("/checkIn")
    public Result<CheckInResultVO> checkIn(@RequestBody CheckInDTO checkInDTO) {
        Long employeeId = BaseContext.getCurrentId();
        log.info("办理入住，员工ID：{}，订单ID：{}", employeeId, checkInDTO.getOrderId());
        
        CheckInResultVO checkInResult = checkInService.checkIn(employeeId, checkInDTO);
        return Result.success(checkInResult);
    }
    @GetMapping("/checkIn/list")
    public Result<PageResult> list(CheckInQueryDTO checkInQueryDTO) {
        log.info("查询入住记录，查询条件：{}", checkInQueryDTO);

        PageResult pageResult = checkInService.pageQuery(checkInQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/checkIn/{id}")
    public Result<CheckInDetailVO> getDetail(@PathVariable Long id) {
        log.info("获取入住详情，ID：{}", id);

        CheckInDetailVO checkInDetail = checkInService.getDetail(id);
        return Result.success(checkInDetail);
    }
}