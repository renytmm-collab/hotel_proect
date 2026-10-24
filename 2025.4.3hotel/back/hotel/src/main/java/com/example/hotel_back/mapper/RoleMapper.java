package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.RolePageQueryDTO;
import com.example.hotel_back.Pojo.Entity.Role;
import com.example.hotel_back.Pojo.VO.RoleNameVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper {
    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    @Select("select * from role where id = #{id}")
    Role getById(Long id);
    
    /**
     * 根据角色名查询角色
     * @param name 角色名
     * @return 角色信息
     */
    @Select("select * from role where role_name = #{name}")
    Role getByName(String name);
    
    /**
     * 添加角色
     * @param role 角色信息
     */
    @Insert("insert into role (role_name, description, status, create_time, update_time) " +
            "values (#{roleName}, #{description}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insert(Role role);

    Page<Role> rolePageQuery(RolePageQueryDTO rolePageQueryDTO);
    /**
     * 更新角色
     * @param role 角色信息
     */
    void update(Role role);
    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    List<RoleNameVO> getAllRoles();
}