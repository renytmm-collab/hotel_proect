package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckInDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckInQueryDTO;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInDetailVO;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInResultVO;

public interface CheckInService {
    
    /**
     * 办理入住
     * @param employeeId 员工ID
     * @param checkInDTO 入住信息
     * @return 入住结果
     */
    CheckInResultVO checkIn(Long employeeId, CheckInDTO checkInDTO);
    /**
     * 分页查询入住记录
     * @param checkInQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(CheckInQueryDTO checkInQueryDTO);
    /**
     * 获取入住详情
     * @param id 入住记录ID
     * @return 入住详情
     */
    CheckInDetailVO getDetail(Long id);
}