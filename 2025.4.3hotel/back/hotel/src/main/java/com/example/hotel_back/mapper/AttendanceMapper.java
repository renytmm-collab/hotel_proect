package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.AttendanceQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.EAttendanceQueryDTO;
import com.example.hotel_back.Pojo.Entity.Attendance;
import com.example.hotel_back.Pojo.VO.AttendanceVO;
import com.example.hotel_back.Pojo.VO.employeevo.EAttendanceVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;

@Mapper
public interface AttendanceMapper {
    /**
     * 根据员工ID和日期查询考勤记录
     * @param employeeId 员工ID
     * @param date 日期
     * @return 考勤记录
     */
    @Select("select * from attendance where employee_id = #{employeeId} and ATTENDANCE_DATE = TRUNC(#{date,jdbcType=DATE})")
    Attendance getByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
    
    /**
     * 添加考勤记录
     * @param attendance 考勤记录
     */
    @Insert("insert into attendance (ID,employee_id, ATTENDANCE_DATE, check_in_time, status, create_time, update_time) " +
            "values (ATTENDANCE_SEQ.nextval,#{employeeId}, #{date}, #{checkInTime}, #{status}, SYSDATE,SYSDATE)")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insert(Attendance attendance);
    /**
     * 更新考勤记录
     * @param attendance 考勤记录
     */
    @Update("update attendance set check_out_time = SYSDATE, status = #{status}, update_time = SYSDATE where id = #{id}")
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void update(Attendance attendance);
    /**
     * 分页查询考勤记录
     * @param attendanceQueryDTO 查询条件
     * @return 考勤记录列表
     */
    Page<AttendanceVO> pageQuery(AttendanceQueryDTO attendanceQueryDTO);
    /**
     * 分页查询考勤记录
     * @param queryDTO 查询条件
     * @return 考勤记录列表
     */
    Page<EAttendanceVO> PageQuery(EAttendanceQueryDTO queryDTO);
    /**
     * 根据ID查询考勤记录
     * @param id 考勤记录ID
     * @return 考勤记录
     */
    Attendance getById(Long id);

    /**
     * 更新考勤状态
     * @param id 考勤记录ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

}