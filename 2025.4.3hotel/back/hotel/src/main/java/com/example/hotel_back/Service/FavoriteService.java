package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.userdao.FavoriteDTO;

public interface FavoriteService {
    
    /**
     * 添加收藏
     * @param userId 用户ID
     * @param favoriteDTO 收藏信息
     */
    void addFavorite(Long userId, FavoriteDTO favoriteDTO);
    /**
     * 取消收藏
     * @param userId 用户ID
     * @param favoriteDTO 收藏信息
     */
    void cancelFavorite(Long userId, FavoriteDTO favoriteDTO);
    /**
     * 分页查询用户收藏
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @return 分页结果
     */
    PageResult pageQuery(Long userId, int pageNum, int pageSize);
}