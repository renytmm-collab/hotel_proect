package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.RoleAddDTO;
import com.example.hotel_back.Pojo.DAO.RolePageQueryDTO;
import com.example.hotel_back.Pojo.DAO.RoleUpdateDTO;
import com.example.hotel_back.Pojo.VO.RoleNameVO;
import com.example.hotel_back.Pojo.VO.RoleVO;

import java.util.List;

public interface RoleService {
    /**
     * 添加角色
     * @param roleAddDTO 角色信息
     */
    void add(RoleAddDTO roleAddDTO);
    /**
     * 分页查询角色信息
     * @param rolePageQueryDTO 角色信息
     * @return 角色信息
     */
    PageResult rolePageQuery(RolePageQueryDTO rolePageQueryDTO);
    /**
     * 更新角色信息
     * @param roleUpdateDTO 角色信息
     */
    void update(RoleUpdateDTO roleUpdateDTO);
    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    List<RoleNameVO> getAllRoles();
}