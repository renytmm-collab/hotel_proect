package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.EmployeePageQueryDTO;
import com.example.hotel_back.Pojo.Entity.Employee;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper {

    /**
     * @param empno
     * @return
     */

    @Select("select * from employee where emp_no = #{empno}")
    Employee getByEmpNo(String empno);
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
    @Select("select * from employee where phone = #{phone}")
    Employee getByPhone(String phone);


    @Insert("insert into employee (real_name, password, phone, sex, emp_no, create_time, update_time, status, role_id) " +
            "values " +
            "(#{realName}, #{password}, #{phone}, #{sex}, #{empNo}, NOW(), NOW(), #{status}, #{roleId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insert(Employee employee);

    Page<Employee> employeePageQuery(EmployeePageQueryDTO queryDTO);

    void adminUpdate(Employee employee);
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updatePassword(Employee employee);
    /**
     * 更新员工信息
     * @param employee 员工信息
     */
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updateEmployee(Employee employee);
}