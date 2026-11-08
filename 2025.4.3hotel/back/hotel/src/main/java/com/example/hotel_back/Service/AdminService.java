package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.*;
import com.example.hotel_back.Pojo.Entity.Employee;
import com.example.hotel_back.Pojo.VO.EmployeeInfoVO;
import org.springframework.stereotype.Service;


public interface AdminService {
    /**
     * 员工登录
     * @param employeeLoginDTO 员工登录信息
     * @return 员工信息
     */

    Employee login(EmployeeLoginDTO employeeLoginDTO);
/**
     * 增加员工
     * @param employeeDTO 员工信息
     */
    void save(EmployeeDTO employeeDTO);
/**
     * 员工分页查询
     * @param employeePageQueryDTO 员工分页查询信息
     * @return 员工分页查询结果
     */
    PageResult employeePageQuery(EmployeePageQueryDTO employeePageQueryDTO);
/**
     * 员工修改信息
     * @param employeeUpdateDTO 员工信息
     */
    void adminUpdateInfo(Long employeeId, EmployeeUpdateDTO employeeUpdateDTO);
    /**
     * 员工查询个人信息
     * @param employeeId 员工id
     * @return 员工信息
     */
    EmployeeInfoVO getEmployeeInfo(Long employeeId);
    /**
     * 修改密码
     * @param passwordUpdateDTO 员工信息
     */
    void updatePassword(Long employeeId, PasswordUpdateDTO passwordUpdateDTO);
    /**
     * 查询员工信息
     * @param employeeId 员工id
     * @return 员工信息
     */
    EmployeeInfoVO getEmployeeInfoById(Long employeeId);
    /**
     * 修改员工信息
     * @param employeeUpdateAdminDTO 员工信息
     */
    void updateEmployee(EmployeeUpdateAdminDTO employeeUpdateAdminDTO);
}
