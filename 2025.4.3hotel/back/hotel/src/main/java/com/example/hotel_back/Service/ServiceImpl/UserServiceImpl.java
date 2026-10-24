package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.constant.MessageConstant;
import com.example.hotel_back.Common.constant.StatusConstant;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.UserPageQueryDTO;
import com.example.hotel_back.Pojo.DAO.UserRegisterDTO;
import com.example.hotel_back.Pojo.DAO.UserStatusUpdateDTO;
import com.example.hotel_back.Pojo.DAO.userdao.UserLoginDTO;
import com.example.hotel_back.Pojo.DAO.userdao.UserRealAuthDTO;
import com.example.hotel_back.Pojo.DAO.userdao.UserUpdateDTO;
import com.example.hotel_back.Pojo.DAO.userdao.UserPasswordDTO;
import com.example.hotel_back.Pojo.Entity.Employee;
import com.example.hotel_back.Pojo.Entity.User;
import com.example.hotel_back.Service.UserService;
import com.example.hotel_back.mapper.UserMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.example.hotel_back.Pojo.VO.uservo.UserVO;


import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void register(UserRegisterDTO userRegisterDTO) {
        // 检查手机号是否已注册
        User existingUser = userMapper.getByPhone(userRegisterDTO.getPhone());
        if (existingUser != null) {
            throw new BusinessException("该手机号已注册");
        }
        
        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user);
        
        // 密码加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        user.setPassword(encryptedPassword);
        
        // 设置默认状态为正常
        user.setStatus(StatusConstant.ENABLE);
        
        // 保存用户
        userMapper.insert(user);
    }
    public User login(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        String password = userLoginDTO.getPassword();



         User user = userMapper.getByPhone(phone);

        // 用户不存在
        if (user == null) {
            throw new BusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        // 密码错误
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new BusinessException(MessageConstant.PASSWORD_ERROR);
        }

        return user;
    }
    public UserVO getUserInfo(Long userId) {
        // 查询用户信息
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 转换为VO对象
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return userVO;
    }
    @Transactional
    public void updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO) {
        // 查询用户是否存在
        User existingUser = userMapper.getById(userId);
        if (existingUser == null) {
            throw new BusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 如果修改了手机号，需要检查新手机号是否已被使用
        if (userUpdateDTO.getPhone() != null && !userUpdateDTO.getPhone().equals(existingUser.getPhone())) {
            User userWithSamePhone = userMapper.getByPhone(userUpdateDTO.getPhone());
            if (userWithSamePhone != null) {
                throw new BusinessException("该手机号已被使用");
            }
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateDTO,user);
        user.setId(userId);

        // 更新用户信息
        userMapper.updateUser(user);
    }
    @Transactional
    public void realAuth(Long userId, UserRealAuthDTO userRealAuthDTO) {
        // 查询用户是否存在
        User existingUser = userMapper.getById(userId);
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户是否已实名认证
        if (existingUser.getRealName() != null && !existingUser.getRealName().isEmpty()
                && existingUser.getIdCard() != null && !existingUser.getIdCard().isEmpty()) {
            throw new BusinessException("您已完成实名认证，不能重复认证");
        }

        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(userRealAuthDTO, user);
        user.setId(userId);
        // 更新用户信息
        userMapper.updateRealAuth(user);
    }
    @Transactional
    public void updatePassword(Long userId, UserPasswordDTO userPasswordDTO) {
        // 查询用户是否存在
        User existingUser = userMapper.getById(userId);
        if (existingUser == null) {
            throw new BusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 校验旧密码
        String encryptedOldPassword = DigestUtils.md5DigestAsHex(userPasswordDTO.getOldPassword().getBytes(StandardCharsets.UTF_8));
        if (!existingUser.getPassword().equals(encryptedOldPassword)) {
            throw new BusinessException(MessageConstant.PASSWORD_EDIT_FAILED);
        }

        // 加密新密码
        String encryptedNewPassword = DigestUtils.md5DigestAsHex(userPasswordDTO.getNewPassword().getBytes(StandardCharsets.UTF_8));
       User user = new User();
        user.setId(userId);
        user.setPassword(encryptedNewPassword);
        // 更新密码
        userMapper.updatePassword(user);
    }
    public PageResult pageQuery(UserPageQueryDTO userPageQueryDTO){
        PageHelper.startPage(userPageQueryDTO.getPageNum(), userPageQueryDTO.getPageSize());
        Page<User> page = userMapper.userPageQuery(userPageQueryDTO);
        long total = page.getTotal(); // 总记录数
        List<User> records = page.getResult(); // 当前页数据
        return new PageResult(total, records);
    }
    public void updateStatus(UserStatusUpdateDTO userStatusUpdateDTO) {
        // 根据ID查询用户
        User user = userMapper.getById(userStatusUpdateDTO.getId());

        // 如果用户不存在，抛出业务异常
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 设置新的状态和更新时间
        user.setStatus(userStatusUpdateDTO.getStatus());


        // 更新用户状态
        userMapper.updateStatus(user);
    }
}