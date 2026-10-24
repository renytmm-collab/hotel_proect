package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.userdao.FavoriteDTO;
import com.example.hotel_back.Pojo.Entity.Favorite;
import com.example.hotel_back.Pojo.Entity.RoomType;
import com.example.hotel_back.Pojo.VO.uservo.FavoriteListVO;
import com.example.hotel_back.Service.FavoriteService;
import com.example.hotel_back.mapper.FavoriteMapper;
import com.example.hotel_back.mapper.RoomTypeMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {
    
    @Autowired
    private FavoriteMapper favoriteMapper;
    
    @Autowired
    private RoomTypeMapper roomTypeMapper;
    @Transactional
    public void addFavorite(Long userId, FavoriteDTO favoriteDTO) {
        // 检查房型是否存在
        RoomType roomType = roomTypeMapper.getById(favoriteDTO.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }
        
        // 检查是否已收藏
        int count = favoriteMapper.checkExist(userId, favoriteDTO.getRoomTypeId());
        if (count > 0) {
            throw new BusinessException("已收藏该房型，请勿重复收藏");
        }
        
        // 创建收藏对象
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setRoomTypeId(favoriteDTO.getRoomTypeId());
        favorite.setCreateTime(LocalDateTime.now());
        
        // 保存收藏
        favoriteMapper.insert(favorite);
        
        log.info("添加收藏成功，用户ID：{}，房型ID：{}", userId, favoriteDTO.getRoomTypeId());
    }
    @Transactional
    public void cancelFavorite(Long userId, FavoriteDTO favoriteDTO) {
        // 检查房型是否存在
        RoomType roomType = roomTypeMapper.getById(favoriteDTO.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }

        // 检查是否已收藏
        int count = favoriteMapper.checkExist(userId, favoriteDTO.getRoomTypeId());
        if (count == 0) {
            throw new BusinessException("未收藏该房型，无法取消");
        }

        // 删除收藏
        int rows = favoriteMapper.delete(userId, favoriteDTO.getRoomTypeId());
        if (rows == 0) {
            throw new BusinessException("取消收藏失败");
        }

        log.info("取消收藏成功，用户ID：{}，房型ID：{}", userId, favoriteDTO.getRoomTypeId());
    }
    @Override
    public PageResult pageQuery(Long userId, int pageNum, int pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);

        // 查询收藏列表
        Page<FavoriteListVO> page = favoriteMapper.pageQuery(userId);

        // 封装结果
        return new PageResult(page.getTotal(), page.getResult());
    }
}