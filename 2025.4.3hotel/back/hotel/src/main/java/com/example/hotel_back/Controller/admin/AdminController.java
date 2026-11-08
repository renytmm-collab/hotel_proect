package com.example.hotel_back.Controller.admin;
import com.example.hotel_back.Common.constant.JwtClaimsConstant;
import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.properties.JwtProperties;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.EmployeeDTO;
import com.example.hotel_back.Pojo.DAO.EmployeeLoginDTO;
import com.example.hotel_back.Pojo.DAO.EmployeePageQueryDTO;
import com.example.hotel_back.Pojo.DAO.EmployeeUpdateDTO;
import com.example.hotel_back.Pojo.Entity.Employee;
import com.example.hotel_back.Pojo.VO.EmployeeInfoVO;
import com.example.hotel_back.Pojo.DAO.EmployeeUpdateAdminDTO;
import com.example.hotel_back.Service.AdminService;
import com.example.hotel_back.Common.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.hotel_back.Pojo.VO.EmployeeLoginVO;
import com.example.hotel_back.Utils.JwtUtil;
import com.example.hotel_back.Pojo.DAO.PasswordUpdateDTO;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtProperties jwtProperties;

    // 方法上增加更明确的关于是管理端接口的描述
    @PostMapping("/login")

    public Result<EmployeeLoginVO> login(@RequestBody  EmployeeLoginDTO employeeLoginDTO) {
        log.info("管理端员工登录：{}", employeeLoginDTO);

        Employee employee = adminService.login(employeeLoginDTO);

        // 登录成功后，生成 jwt 令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        // 使用更新后的JwtUtil创建JWT令牌
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .realName(employee.getRealName())
                .roleId(employee.getRoleId())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }
    @PostMapping("/logout")
    public Result<String> logout() {
         BaseContext.removeCurrentId();
        return Result.success();
    }
    @PostMapping("/employee/add")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工：{}",employeeDTO);
        adminService.save(employeeDTO);
        return Result.success();
    }
    @GetMapping("/employee/list")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询，参数为：{}", employeePageQueryDTO);
        PageResult pageResult = adminService.employeePageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }
    @PutMapping("/updateInfo")
    public Result<String> updateInfo(@RequestBody EmployeeUpdateDTO employeeUpdateDTO) {
        log.info("修改个人信息：{}", employeeUpdateDTO);

        // 获取当前登录员工ID
        Long employeeId = BaseContext.getCurrentId();
        if (employeeId == null) {
            return Result.error("未登录");
        }
        adminService.adminUpdateInfo(employeeId, employeeUpdateDTO);
        return Result.success();
    }
    @GetMapping("/info")
    public Result<EmployeeInfoVO> getInfo() {
        // 获取当前登录员工ID
        Long employeeId = BaseContext.getCurrentId();

        EmployeeInfoVO employeeInfo = adminService.getEmployeeInfo(employeeId);
        return Result.success(employeeInfo);
    }

    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        log.info("修改密码");

        // 获取当前登录员工ID
        Long employeeId = BaseContext.getCurrentId();
        if (employeeId == null) {
            return Result.error("未登录");
        }

        adminService.updatePassword(employeeId, passwordUpdateDTO);
        return Result.success();
    }
    @GetMapping("/employee/info/{id}")
    public Result<EmployeeInfoVO> getEmployeeInfo(@PathVariable Long id) {
        log.info("获取员工详细信息，员工ID：{}", id);

        // 检查参数
        if (id == null) {
            return Result.error("员工ID不能为空");
        }

        EmployeeInfoVO employeeInfo = adminService.getEmployeeInfoById(id);
        return Result.success(employeeInfo);
    }
    @PutMapping("/employee/update")
    public Result<String> updateEmployee(@RequestBody EmployeeUpdateAdminDTO employeeUpdateAdminDTO) {
        log.info("修改员工信息：{}", employeeUpdateAdminDTO);

        // 检查参数
        if (employeeUpdateAdminDTO.getId() == null) {
            return Result.error("员工ID不能为空");
        }

        adminService.updateEmployee(employeeUpdateAdminDTO);
        return Result.success();
    }
}

