package com.example.hotel_back.Controller.admin;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.UserPageQueryDTO;
import com.example.hotel_back.Pojo.DAO.UserStatusUpdateDTO;
import com.example.hotel_back.Pojo.VO.uservo.UserVO;
import com.example.hotel_back.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@Slf4j
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 用户分页查询
     * @param userPageQueryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult> list(UserPageQueryDTO userPageQueryDTO) {
        log.info("用户分页查询，参数：{}", userPageQueryDTO);
        PageResult pageResult = userService.pageQuery(userPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 获取员工详细信息
     * @param id 员工ID
     * @return 员工详细信息
     */
    @GetMapping("/info/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        log.info("根据ID查询员工信息：{}", id);
        UserVO userVO = userService.getUserInfo(id);
        return Result.success(userVO);
    }
    @PutMapping("/update")
    public Result<String> updateStatus(@RequestBody UserStatusUpdateDTO userStatusUpdateDTO) {
        log.info("修改用户账号状态：{}", userStatusUpdateDTO);

        userService.updateStatus(userStatusUpdateDTO);
        return Result.success();
    }
}