package com.example.hotel_back.Controller.employee;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.AttendanceQueryDTO;
import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.VO.employeevo.AttendanceStatusVO;
import com.example.hotel_back.Service.AttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeAttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/checkIn")
    public Result<String> checkIn() {
        Long employeeId = BaseContext.getCurrentId();
        log.info("员工签到，员工ID：{}", employeeId);
        
        attendanceService.checkIn(employeeId);
        return Result.success();
    }
    @PostMapping("/checkOut")
    public Result<String> checkOut() {
        Long employeeId = BaseContext.getCurrentId();
        log.info("员工签退，员工ID：{}", employeeId);

        attendanceService.checkOut(employeeId);
        return Result.success();
    }
    @GetMapping("/attendance/list")
    public Result<PageResult> list(AttendanceQueryDTO attendanceQueryDTO) {
        Long employeeId = BaseContext.getCurrentId();
        // 设置员工ID
        attendanceQueryDTO.setId(employeeId);
        PageResult pageResult = attendanceService.pageQuery(attendanceQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/attendance/status")
    public Result<AttendanceStatusVO> getAttendanceStatus() {
        // 自动获取当前日期
        LocalDate today = LocalDate.now();

        // 从JWT中获取当前登录员工ID
        Long employeeId = BaseContext.getCurrentId();
        log.info("查询员工签到状态，员工ID：{}，日期：{}", employeeId, today);

        // 查询签到状态
        Integer status = attendanceService.getAttendanceStatus(employeeId, today);

        // 如果状态为null，设置为-1（未签到未签退）
        if (status == null) {
            status = -1;
        }

        return Result.success(new AttendanceStatusVO(status));
    }
}