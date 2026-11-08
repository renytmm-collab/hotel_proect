package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckOutDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckOutQueryDTO;
import com.example.hotel_back.Pojo.VO.employeevo.CheckOutListVO;

public interface CheckOutService {
    
    /**
     * 办理退房
     * @param employeeId 员工ID
     * @param checkOutDTO 退房信息
     */
    void handleCheckOut(Long employeeId, CheckOutDTO checkOutDTO);
    /**
     * 分页查询结算记录
     * @param checkOutQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(CheckOutQueryDTO checkOutQueryDTO);
    /**
     * 获取结算详情
     * @param id 结算记录ID
     * @return 结算详情
     */
    CheckOutListVO getDetail(Long id);
}