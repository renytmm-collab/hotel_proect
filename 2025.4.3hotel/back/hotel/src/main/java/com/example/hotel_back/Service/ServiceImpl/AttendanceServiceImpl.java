package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.constant.AttendanceConstant;
import com.example.hotel_back.Common.constant.AttendanceTypeConstant;
import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.AttendanceQueryDTO;
import com.example.hotel_back.Pojo.DAO.AttendanceStatusUpdateDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.EAttendanceQueryDTO;
import com.example.hotel_back.Pojo.Entity.Attendance;
import com.example.hotel_back.Pojo.Entity.Employee;
import com.example.hotel_back.Pojo.VO.AttendanceVO;
import com.example.hotel_back.Pojo.VO.employeevo.EAttendanceVO;
import com.example.hotel_back.Service.AttendanceService;
import com.example.hotel_back.mapper.AdminMapper;
import com.example.hotel_back.mapper.AttendanceMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceMapper attendanceMapper;
    
    @Autowired
    private AdminMapper adminMapper;

    @Transactional
    public void checkIn(Long employeeId) {
        // 检查员工是否存在
        Employee employee = adminMapper.getById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        // 获取当前日期和时间
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        
        // 检查今天是否已经签到
        Attendance existingAttendance = attendanceMapper.getByEmployeeIdAndDate(employeeId, today);
        
        if (existingAttendance != null) {
            // 已经签到过，不能重复签到
            throw new BusinessException("今天已经签到过了");
        }
        
        // 创建新的考勤记录
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setDate(today);
        attendance.setCheckInTime(now);
        
        // 判断签到状态
        int status;
        if (now.toLocalTime().isAfter(AttendanceConstant.WORK_START_TIME)) {
            // 迟到
            status = AttendanceTypeConstant.LATE;
        } else {

            status = AttendanceTypeConstant.SIGNED_IN;
        }
        
        attendance.setStatus(status);
        
        // 保存考勤记录
        attendanceMapper.insert(attendance);
        
        log.info("员工签到成功，员工ID：{}，状态：{}", employeeId, status);
    }

    @Override
    @Transactional
    public void checkOut(Long employeeId) {
        // 检查员工是否存在
        Employee employee = adminMapper.getById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 获取当前日期和时间
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 查询今天的签到记录
        Attendance attendance = attendanceMapper.getByEmployeeIdAndDate(employeeId, today);

        if (attendance == null) {
            // 没有签到记录，不能签退
            throw new BusinessException("今天还没有签到，不能签退");
        }

        if (attendance.getCheckOutTime() != null) {
            // 已经签退过，不能重复签退
            throw new BusinessException("今天已经签退过了");
        }

        // 判断签退状态
        int status = attendance.getStatus();
        if (status != AttendanceTypeConstant.LEAVE) { // 如果不是请假状态
            if (now.toLocalTime().isBefore(AttendanceConstant.WORK_END_TIME)) {
                // 早退
                status = 2;
            } else if (status != 1) { // 如果不是迟到状态
                // 正常
                status = 0;
            }
            // 如果是迟到状态，保持迟到状态不变
        }

        attendance.setStatus(status);

        // 更新考勤记录
        attendanceMapper.update(attendance);

        log.info("员工签退成功，员工ID：{}，状态：{}", employeeId, status);
    }
    public PageResult pageQuery(AttendanceQueryDTO attendanceQueryDTO) {
        // 开启分页
        PageHelper.startPage(attendanceQueryDTO.getPageNum(), attendanceQueryDTO.getPageSize());
        attendanceQueryDTO.setId(BaseContext.getCurrentId());
        // 执行查询
        Page<AttendanceVO> page = attendanceMapper.pageQuery(attendanceQueryDTO);

        // 获取结果
        long total = page.getTotal();
        List<AttendanceVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    public PageResult pageQuery(EAttendanceQueryDTO queryDTO) {
        // 开启分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 执行查询
        Page<EAttendanceVO> page = attendanceMapper.PageQuery(queryDTO);

        // 获取结果
        long total = page.getTotal();
        List<EAttendanceVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    public void updateStatus(AttendanceStatusUpdateDTO updateDTO) {
        // 查询考勤记录是否存在
        Attendance attendance = attendanceMapper.getById(updateDTO.getId());
        if (attendance == null) {
            throw new BusinessException("考勤记录不存在");
        }

        // 验证状态值是否合法
        Integer status = updateDTO.getStatus();
        if (status == null || status < 0 || status > 4) { // 状态范围是0-4
            throw new BusinessException("考勤状态值不合法");
        }

        // 更新考勤状态
        attendanceMapper.updateStatus(updateDTO.getId(), status);

        log.info("考勤状态更新成功，考勤ID：{}，状态：{}", updateDTO.getId(), status);
    }
    public Integer getAttendanceStatus(Long employeeId, LocalDate date) {
        // 查询员工当日签到记录
        Attendance attendance = attendanceMapper.getByEmployeeIdAndDate(employeeId, date);

        // 如果没有记录，返回null
        if (attendance == null) {
            return null;
        }




        return attendance.getStatus();
    }
}