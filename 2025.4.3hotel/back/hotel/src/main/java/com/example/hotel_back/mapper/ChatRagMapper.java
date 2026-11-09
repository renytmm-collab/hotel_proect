package com.example.hotel_back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 智能客服 RAG 检索 Mapper
 * 从数据库实时检索知识（房型、空房、评价），供大模型结合真实数据回答。
 */
@Mapper
public interface ChatRagMapper {

    /**
     * 查询所有在售房型（status=1 上架），作为房价/房型知识
     */
    @Select("SELECT type_name AS typeName, price, area, bed_type AS bedType, " +
            "max_guests AS maxGuests, facilities, description " +
            "FROM room_type WHERE status = 1 ORDER BY weight DESC, price ASC")
    List<Map<String, Object>> listRoomTypes();

    /**
     * 查询当前空闲房间数量（room.status=1 空闲），按房型统计
     */
    @Select("SELECT rt.type_name AS typeName, COUNT(r.id) AS freeCount " +
            "FROM room r JOIN room_type rt ON r.room_type_id = rt.id " +
            "WHERE r.status = 1 GROUP BY rt.type_name, rt.price ORDER BY rt.price ASC")
    List<Map<String, Object>> countFreeRooms();

    /**
     * 查询最近显示中的住客评价（review.status=1），作为口碑知识
     */
    @Select("SELECT content, score FROM review WHERE status = 1 ORDER BY create_time DESC LIMIT 5")
    List<Map<String, Object>> listRecentReviews();
}
