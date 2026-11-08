package com.example.hotel_back.Controller.user;

import com.example.hotel_back.Common.constant.JwtClaimsConstant;
import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.properties.JwtProperties;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.UserRegisterDTO;
import com.example.hotel_back.Pojo.DAO.userdao.*;
import com.example.hotel_back.Pojo.Entity.User;
import com.example.hotel_back.Pojo.VO.uservo.UserLoginVO;
import com.example.hotel_back.Pojo.VO.uservo.UserVO;
import com.example.hotel_back.Service.UserService;
import com.example.hotel_back.Utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO);
        userService.register(userRegisterDTO);
        return Result.success();
    }
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);

        User user = userService.login(userLoginDTO);
        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        // 构建登录结果
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .sex(user.getSex())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }
    @GetMapping("/info")
    public Result<UserVO> info() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取用户信息，用户ID：{}", userId);

        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }
    @PutMapping("/update")

    public Result<String> update(@RequestBody UserUpdateDTO userUpdateDTO) {

        Long userId = BaseContext.getCurrentId();
        log.info("修改用户信息，用户ID：{}，修改信息：{}", userId, userUpdateDTO);

        userService.updateUserInfo(userId, userUpdateDTO);
        return Result.success();
    }
    @PostMapping("/real")
    public Result<String> realAuth(@RequestBody UserRealAuthDTO userRealAuthDTO) {
        // 从JWT中获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("用户实名认证，用户ID：{}，认证信息：{}", userId, userRealAuthDTO);

        userService.realAuth(userId, userRealAuthDTO);
        return Result.success();
    }
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody UserPasswordDTO userPasswordDTO) {

        Long userId = BaseContext.getCurrentId();
        log.info("修改密码，用户ID：{}", userId);


        userService.updatePassword(userId, userPasswordDTO);
        return Result.success();
    }

}