package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.UserPageQueryDTO;
import com.example.hotel_back.Pojo.Entity.User;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("select * from users where phone = #{phone}")
    User getByPhone(String phone);
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    @Select("select * from users where id = #{id}")
    User getById(Long id);
    
    /**
     * 添加用户
     * @param user 用户信息
     */
    @Insert("insert into users (username, phone, password, status, create_time, update_time) " +
            "values (#{username}, #{phone}, #{password}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insert(User user);
    /**
     * 更新用户信息
     * @param user 用户信息
     */
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updateUser(User user);
    /**
     * 更新用户实名认证信息
     * @param user 用户信息
     */
    @Update("update users set real_name = #{realName}, id_card = #{idCard}, update_time = NOW() where id = #{id}")
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updateRealAuth(User user);

    @Update("update users set password = #{password}, update_time = NOW() where id = #{id}")
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updatePassword(User user);

    @Update("update users set status = #{status}, update_time = NOW() where id = #{id}")
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updateStatus(User user);
    /**
     * 根据用户ID列表查询用户信息
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    @MapKey("id")
    List<Map<String, Object>> getByUserIds(@Param("list") List<Long> userIds);

    Page<User> userPageQuery(UserPageQueryDTO queryDTO);
}