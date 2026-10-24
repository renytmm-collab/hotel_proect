package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.PerformanceAddDTO;
import com.example.hotel_back.Pojo.DAO.PerformanceQueryDTO;
import com.example.hotel_back.Pojo.DAO.PerformanceUpdateDTO;
import com.example.hotel_back.Pojo.Entity.Employee;
import com.example.hotel_back.Pojo.Entity.Performance;
import com.example.hotel_back.Pojo.VO.PerformanceVO;
import com.example.hotel_back.Service.PerformanceService;
import com.example.hotel_back.mapper.AdminMapper;
import com.example.hotel_back.mapper.PerformanceMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Slf4j
public class PerformanceServiceImpl implements PerformanceService {

    @Autowired
    private PerformanceMapper performanceMapper;
    
    @Autowired
    private AdminMapper employeeMapper;

    @Override
    public void add(Long adminId, PerformanceAddDTO performanceAddDTO) {
        // 验证员工是否存在
        Employee employee = employeeMapper.getById(performanceAddDTO.getEmployeeId());
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        
        // 验证月份格式是否正确
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            formatter.parse(performanceAddDTO.getMonth());
        } catch (DateTimeParseException e) {
            throw new BusinessException("月份格式不正确，应为yyyy-MM");
        }
        
        // 验证分数范围
        if (performanceAddDTO.getScore().doubleValue() < 0 || performanceAddDTO.getScore().doubleValue() > 100) {
            throw new BusinessException("绩效分数应在0-100之间");
        }
        
        // 检查是否已存在该员工当月的绩效记录
        Performance existingPerformance = performanceMapper.getByEmployeeIdAndMonth(
            performanceAddDTO.getEmployeeId(), 
            performanceAddDTO.getMonth()
        );
        
        if (existingPerformance != null) {
            throw new BusinessException("该员工当月已有绩效记录");
        }
        
        // 创建绩效记录
        Performance performance = new Performance();
        performance.setEmployeeId(performanceAddDTO.getEmployeeId());
        performance.setMonth(performanceAddDTO.getMonth());
        performance.setScore(performanceAddDTO.getScore());
        performance.setEvaluation(performanceAddDTO.getEvaluation());
        performance.setEvaluatorId(adminId);
        
        // 保存绩效记录
        performanceMapper.insert(performance);
        
        log.info("添加绩效评估成功，管理员ID：{}，员工ID：{}，月份：{}", 
                adminId, performanceAddDTO.getEmployeeId(), performanceAddDTO.getMonth());
    }
    public PageResult pageQuery(PerformanceQueryDTO queryDTO) {
        // 开启分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 执行查询
        Page<PerformanceVO> page = performanceMapper.pageQuery(queryDTO);

        // 获取结果
        long total = page.getTotal();
        List<PerformanceVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    public void update(Long adminId, PerformanceUpdateDTO updateDTO) {
        // 查询绩效记录是否存在
        Performance performance = performanceMapper.getById(updateDTO.getId());
        if (performance == null) {
            throw new BusinessException("绩效记录不存在");
        }

        // 验证分数范围
        if (updateDTO.getScore().doubleValue() < 0 || updateDTO.getScore().doubleValue() > 100) {
            throw new BusinessException("绩效分数应在0-100之间");
        }
        BeanUtils.copyProperties(updateDTO, performance);

        // 更新绩效记录
        performanceMapper.update(performance);

        log.info("修改绩效评估成功，管理员ID：{}，绩效ID：{}", adminId, updateDTO.getId());
    }
}