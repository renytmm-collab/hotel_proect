package com.example.hotel_back.Service.ServiceImpl;
import com.example.hotel_back.Pojo.DAO.*;
import com.example.hotel_back.mapper.RoleMapper;
import com.example.hotel_back.Common.constant.MessageConstant;
import com.example.hotel_back.Common.constant.PasswordConstant;
import com.example.hotel_back.Common.constant.StatusConstant;
import com.example.hotel_back.Common.exception.AccountLockedException;
import com.example.hotel_back.Common.exception.AccountNotFoundException;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.exception.PasswordErrorException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.Entity.Employee;
import com.example.hotel_back.Pojo.Entity.Role;
import com.example.hotel_back.Pojo.VO.EmployeeInfoVO;
import com.example.hotel_back.Service.AdminService;
import com.example.hotel_back.mapper.AdminMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private RoleMapper roleMapper;


    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String empno = employeeLoginDTO.getEmpNo();
        String password = employeeLoginDTO.getPassword();

        //1、根据工号查询数据库中的数据
        Employee employee = adminMapper.getByEmpNo(empno);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //密码比对
        //对前端传过来的明文密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 增添员工
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        Employee existingemployee1 = adminMapper.getByEmpNo(employeeDTO.getEmpNo());
        if (existingemployee1 != null) {
            throw new BusinessException("工号已被占用");
        }
        Employee existingemployee2 = adminMapper.getByPhone(employeeDTO.getPhone());
        if (existingemployee2 != null) {
            throw new BusinessException("用户已存在");
        }
        Employee employee = new Employee();


        BeanUtils.copyProperties(employeeDTO, employee);

        //设置账号的状态，默认正常状态 1表示正常 0表示锁定
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码，默认密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        adminMapper.insert(employee);
    }

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult employeePageQuery(EmployeePageQueryDTO employeePageQueryDTO) {

        PageHelper.startPage(employeePageQueryDTO.getPageNum(), employeePageQueryDTO.getPageSize());
        if(employeePageQueryDTO.getBeginTime()!=null){
        employeePageQueryDTO.setStartDate(employeePageQueryDTO.getBeginTime().toLocalDate().toString());}
        if (employeePageQueryDTO.getEndTime()!=null){
        employeePageQueryDTO.setEndDate(employeePageQueryDTO.getEndTime().toLocalDate().toString());}
        Page<Employee> page = adminMapper.employeePageQuery(employeePageQueryDTO);
        long total = page.getTotal(); // 总记录数
        List<Employee> records = page.getResult(); // 当前页数据
        return new PageResult(total, records);
    }
    /**
     * 更新个人信息
     * @param employeeUpdateDTO
     */
    public void adminUpdateInfo(Long employeeId, EmployeeUpdateDTO employeeUpdateDTO) {

        Employee employee =new Employee();
        // 设置更新信息
        BeanUtils.copyProperties(employeeUpdateDTO, employee);
        employee.setId(employeeId);


        adminMapper.adminUpdate(employee);
    }
    /**
     * 查询个人详细信息
     * @param employeeId
     * @return
     */
    public EmployeeInfoVO getEmployeeInfo(Long employeeId) {
        // 查询员工基本信息
        Employee employee = adminMapper.getById(employeeId);
        // 查询角色名称
        String roleName = "";

        if (employee.getRoleId() != null) {
            Role role = roleMapper.getById(employee.getRoleId());
            if (role != null) {
                roleName = role.getRoleName();
            }
        }

        // 封装VO对象
        EmployeeInfoVO employeeInfoVO = new EmployeeInfoVO();
        BeanUtils.copyProperties(employee, employeeInfoVO);
        employeeInfoVO.setRoleName(roleName);
        return employeeInfoVO;
    }
    /**
     * 更新密码
     * @param passwordUpdateDTO
     */
    public void updatePassword(Long employeeId, PasswordUpdateDTO passwordUpdateDTO) {
        // 查询员工是否存在
        Employee employee = adminMapper.getById(employeeId);
        // 校验旧密码
        String password=passwordUpdateDTO.getOldPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            throw new BusinessException(MessageConstant.PASSWORD_EDIT_FAILED);
        }
        // 更新密码
        Employee updateEmployee = new Employee();
        updateEmployee.setId(employeeId);
        updateEmployee.setPassword(DigestUtils.md5DigestAsHex(passwordUpdateDTO.getNewPassword().getBytes()));


        adminMapper.updatePassword(updateEmployee);
    }
    public EmployeeInfoVO getEmployeeInfoById(Long employeeId) {
        // 查询员工基本信息
        Employee employee = adminMapper.getById(employeeId);
        if (employee == null) {
            throw new BusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 查询角色名称
        String roleName = "";
        if (employee.getRoleId() != null) {
            Role role = roleMapper.getById(employee.getRoleId());
            if (role != null) {
                roleName = role.getRoleName();
            }
        }

        // 封装VO对象
        EmployeeInfoVO employeeInfoVO = new EmployeeInfoVO();
        BeanUtils.copyProperties(employee, employeeInfoVO);
        employeeInfoVO.setRoleName(roleName);

        return employeeInfoVO;
    }
    public void updateEmployee(EmployeeUpdateAdminDTO employeeUpdateAdminDTO) {

        // 创建员工对象
        Employee employee = new Employee();
       BeanUtils.copyProperties(employeeUpdateAdminDTO, employee);

        if (employeeUpdateAdminDTO.getPassword() != null && !employeeUpdateAdminDTO.getPassword().isEmpty()) {
            employee.setPassword(DigestUtils.md5DigestAsHex(employeeUpdateAdminDTO.getPassword().getBytes()));
        }

        // 更新员工信息
        adminMapper.updateEmployee(employee);
    }

}
