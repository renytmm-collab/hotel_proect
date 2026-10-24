package com.example.hotel_back.Controller.admin;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.AttendanceStatusUpdateDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.EAttendanceQueryDTO;
import com.example.hotel_back.Service.AttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/attendance")
@Slf4j
public class AdminAttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/list")
    public Result<PageResult> list(EAttendanceQueryDTO queryDTO) {
        log.info("查看员工考勤记录，查询条件：{}", queryDTO);
        
        PageResult pageResult = attendanceService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }
    @PutMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestBody AttendanceStatusUpdateDTO updateDTO) {
        log.info("修改考勤状态，考勤ID：{}，状态：{}", updateDTO.getId(), updateDTO.getStatus());

        attendanceService.updateStatus(updateDTO);
        return Result.success();
    }
}