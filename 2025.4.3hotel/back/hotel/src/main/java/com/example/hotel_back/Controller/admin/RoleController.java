package com.example.hotel_back.Controller.admin;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.RoleAddDTO;
import com.example.hotel_back.Pojo.DAO.RolePageQueryDTO;
import com.example.hotel_back.Pojo.DAO.RoleUpdateDTO;
import com.example.hotel_back.Service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/role")
@Slf4j

public class RoleController {

    @Autowired
    private RoleService roleService;
    @GetMapping("/list")
    public Result<PageResult> list(RolePageQueryDTO rolePageQueryDTO) {
        log.info("分页查询角色列表：{}", rolePageQueryDTO);
        PageResult pageResult = roleService.rolePageQuery(rolePageQueryDTO);
        return Result.success(pageResult);
    }
    @PostMapping("/add")
    public Result<String> add(@RequestBody RoleAddDTO roleAddDTO) {
        log.info("添加角色：{}", roleAddDTO);
        roleService.add(roleAddDTO);
        return Result.success("添加成功");
    }
    @PutMapping("/update")
    public Result<String> update(@RequestBody RoleUpdateDTO roleUpdateDTO) {
        log.info("修改角色：{}", roleUpdateDTO);

        // 检查参数
        if (roleUpdateDTO.getId() == null) {
            return Result.error("角色ID不能为空");
        }

        roleService.update(roleUpdateDTO);
        return Result.success();
    }
}