package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.PerformanceQueryDTO;
import com.example.hotel_back.Pojo.Entity.Performance;
import com.example.hotel_back.Pojo.VO.PerformanceVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PerformanceMapper {
    
    /**
     * 添加绩效记录
     * @param performance 绩效记录
     */
    @Insert("INSERT INTO performance (employee_id, month, score, evaluation, evaluator_id, create_time, update_time) " +
            "VALUES (#{employeeId}, #{month}, #{score}, #{evaluation}, #{evaluatorId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insert(Performance performance);
    
    /**
     * 根据员工ID和月份查询绩效记录
     * @param employeeId 员工ID
     * @param month 月份
     * @return 绩效记录
     */
    Performance getByEmployeeIdAndMonth(@Param("employeeId") Long employeeId, @Param("month") String month);
    /**
     * 分页查询绩效评估
     * @param queryDTO 查询条件
     * @return 绩效评估列表
     */
    Page<PerformanceVO> pageQuery(PerformanceQueryDTO queryDTO);
    /**
     * 根据ID查询绩效记录
     * @param id 绩效记录ID
     * @return 绩效记录
     */
    Performance getById(Long id);

    /**
     *
     * @param performance
     */
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void update(Performance performance);
}