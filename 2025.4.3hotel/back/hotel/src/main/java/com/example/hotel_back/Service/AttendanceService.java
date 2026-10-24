package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.AttendanceQueryDTO;
import com.example.hotel_back.Pojo.DAO.AttendanceStatusUpdateDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.EAttendanceQueryDTO;

import java.time.LocalDate;

public interface AttendanceService {
    /**
     * 员工签到
     * @param employeeId 员工ID
     */
    void checkIn(Long employeeId);
    /**
     * 员工签退
     * @param employeeId 员工ID
     */
    void checkOut(Long employeeId);
    /**
     * 分页查询考勤记录
     * @param attendanceQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(AttendanceQueryDTO attendanceQueryDTO);
    /**
     * 分页查询考勤记录
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(EAttendanceQueryDTO queryDTO);
    /**
     * 修改考勤状态
     * @param updateDTO 更新信息
     */
    void updateStatus(AttendanceStatusUpdateDTO updateDTO);
    /**
     * 获取员工当日签到状态
     * @param employeeId 员工ID
     * @param date 日期
     * @return 签到状态
     * -1：未签到未签退（初始状态）
     * 0：正常（已签到已签退）
     * 1：迟到（已签到未签退）
     * 2：早退（已签到已签退）
     * 3：旷工
     * 4：请假（未签到未签退）
     * 5：已签到未签退
     * null：未找到记录
     */
    Integer getAttendanceStatus(Long employeeId, LocalDate date);
}