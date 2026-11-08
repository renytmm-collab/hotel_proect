package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.constant.StatusConstant;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.RoleAddDTO;
import com.example.hotel_back.Pojo.DAO.RolePageQueryDTO;
import com.example.hotel_back.Pojo.DAO.RoleUpdateDTO;
import com.example.hotel_back.Pojo.Entity.Role;
import com.example.hotel_back.Pojo.VO.RoleNameVO;
import com.example.hotel_back.Service.RoleService;
import com.example.hotel_back.mapper.RoleMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    /**
     *
     * @param rolePageQueryDTO 角色信息
     * @return
     */
    public PageResult rolePageQuery(RolePageQueryDTO rolePageQueryDTO) {
        // 开启分页
        PageHelper.startPage(rolePageQueryDTO.getPage(), rolePageQueryDTO.getPageSize());

        // 执行查询
        Page<Role> page = roleMapper.rolePageQuery(rolePageQueryDTO);

        // 封装结果
        long total = page.getTotal();
        List<Role> records = page.getResult();
        return new PageResult(total, records);


    }
    public void add(RoleAddDTO roleAddDTO) {
        // 检查角色名是否已存在
        Role existingRole = roleMapper.getByName(roleAddDTO.getRoleName());
        if (existingRole != null) {
            throw new BusinessException("角色名已存在");
        }
        // 创建角色对象
        Role role = new Role();
        role.setStatus(StatusConstant.ENABLE);
        BeanUtils.copyProperties(roleAddDTO, role);
        
        // 保存角色
        roleMapper.insert(role);
    }
    public void update(RoleUpdateDTO roleUpdateDTO) {
        // 检查角色名是否重复（排除自身）
        Role roleWithSameName = roleMapper.getByName(roleUpdateDTO.getRoleName());
        if (roleWithSameName != null && !roleWithSameName.getId().equals(roleUpdateDTO.getId())) {
            throw new BusinessException("角色名已存在");
        }

        // 创建角色对象
        Role updatedRole = Role.builder()
                .id(roleUpdateDTO.getId())
                .roleName(roleUpdateDTO.getRoleName())
                .description(roleUpdateDTO.getDescription())
                .status(roleUpdateDTO.getStatus())
                .build();
        // 更新角色
        roleMapper.update(updatedRole);
    }
    public List<RoleNameVO> getAllRoles() {
        // 查询所有角色
        List<RoleNameVO> roleList = roleMapper.getAllRoles();
        log.info("查询所有角色，共{}条记录", roleList.size());
        return roleList;
    }
    }