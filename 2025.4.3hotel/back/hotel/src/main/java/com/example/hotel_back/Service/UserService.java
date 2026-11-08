package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.UserPageQueryDTO;
import com.example.hotel_back.Pojo.DAO.UserRegisterDTO;
import com.example.hotel_back.Pojo.DAO.UserStatusUpdateDTO;
import com.example.hotel_back.Pojo.DAO.userdao.UserLoginDTO;
import com.example.hotel_back.Pojo.DAO.userdao.UserPasswordDTO;
import com.example.hotel_back.Pojo.DAO.userdao.UserRealAuthDTO;
import com.example.hotel_back.Pojo.DAO.userdao.UserUpdateDTO;
import com.example.hotel_back.Pojo.Entity.User;
import com.example.hotel_back.Pojo.VO.uservo.UserVO;

public interface UserService {
    /**
     * 用户注册
     * @param userRegisterDTO 注册信息
     * @return 用户信息
     */
    void register(UserRegisterDTO userRegisterDTO);
    /**
     * 用户登录
     * @param userLoginDTO 登录信息
     * @return 登录结果
     */
    User login(UserLoginDTO userLoginDTO);
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long userId);
    /**
     * 修改用户信息
     * @param userId 用户ID
     * @param userUpdateDTO 用户信息
     */
    void updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO);
    /**
     * 用户实名认证
     * @param userId 用户ID
     * @param userRealAuthDTO 实名认证信息
     */
    void realAuth(Long userId, UserRealAuthDTO userRealAuthDTO);
    /**
     * 修改密码
     * @param userId 用户ID
     * @param userPasswordDTO 密码信息
     */
    void updatePassword(Long userId, UserPasswordDTO userPasswordDTO);
    /**
     * 用户分页查询
     * @param userPageQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(UserPageQueryDTO userPageQueryDTO);
    /**
     * 修改用户账号状态
     * @param userStatusUpdateDTO 用户状态更新DTO
     */
    void updateStatus(UserStatusUpdateDTO userStatusUpdateDTO);
}