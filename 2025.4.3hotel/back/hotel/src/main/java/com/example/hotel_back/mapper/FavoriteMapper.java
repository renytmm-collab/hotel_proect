package com.example.hotel_back.mapper;

import com.example.hotel_back.Pojo.Entity.Favorite;
import com.example.hotel_back.Pojo.VO.uservo.FavoriteListVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

@Mapper
public interface FavoriteMapper {
    
    /**
     * 添加收藏
     * @param favorite 收藏信息
     */
    @Insert("INSERT INTO favorite (id, user_id, room_type_id, create_time) VALUES (FAVORITE_SEQ.nextval, #{userId}, #{roomTypeId}, SYSDATE)")
    void insert(Favorite favorite);
    
    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param roomTypeId 房型ID
     * @return 是否已收藏
     */
    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND room_type_id = #{roomTypeId}")
    int checkExist(Long userId, Long roomTypeId);
    /**
     * 删除收藏
     * @param userId 用户ID
     * @param roomTypeId 房型ID
     * @return 影响行数
     */
    @Delete("DELETE FROM favorite WHERE user_id = #{userId} AND room_type_id = #{roomTypeId}")
    int delete(Long userId, Long roomTypeId);
    /**
     * 分页查询用户收藏
     * @param userId 用户ID
     * @return 收藏列表
     */
    Page<FavoriteListVO> pageQuery(Long userId);
}