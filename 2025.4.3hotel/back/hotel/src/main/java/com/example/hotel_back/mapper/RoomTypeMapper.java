package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.RoomTypePageQueryDTO;
import com.example.hotel_back.Pojo.DAO.userdao.RoomTypeQueryDTO;
import com.example.hotel_back.Pojo.Entity.RoomType;
import com.example.hotel_back.Pojo.Entity.RoomTypeImage;
import com.example.hotel_back.Pojo.VO.RoomTypeNameVO;
import com.example.hotel_back.Pojo.VO.uservo.HotRoomTypeVO;
import com.example.hotel_back.Pojo.VO.uservo.RoomTypeImageVO;
import com.example.hotel_back.Pojo.VO.uservo.RoomTypeVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomTypeMapper {
    /**
     * 根据房型名称查询房型
     * @param typeName 房型名称
     * @return 房型信息
     */
    @Select("select * from room_type where type_name = #{typeName}")
    RoomType getByTypeName(String typeName);
    /**
     * 根据ID查询房型
     * @param id 房型ID
     * @return 房型信息
     */
    @Select("select * from room_type where id = #{id}")
    RoomType getById(Long id);
    
    /**
     * 添加房型
     * @param roomType 房型信息
     */
    @Insert("insert into room_type (id, type_name, price, area, bed_type, max_guests, description, facilities, weight, status, create_time, update_time) " +
            "values (ROOM_TYPE_SEQ.nextval, #{typeName}, #{price}, #{area}, #{bedType}, #{maxGuests}, #{description}, #{facilities}, #{weight}, #{status}, SYSDATE, SYSDATE)")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insert(RoomType roomType);
    /**
     * 分页查询房型
     * @param roomTypePageQueryDTO 查询条件
     * @return 房型列表
     */
    Page<RoomType> pageQuery(RoomTypePageQueryDTO roomTypePageQueryDTO);
    /**
     * 将房型的所有主图设置为非主图
     * @param roomTypeId 房型ID
     */
    @Update("update room_type_image set is_main = 0 where room_type_id = #{roomTypeId} and is_main = 1")
    void updateMainImage(Long roomTypeId);

    /**
     * 添加房型图片
     * @param roomTypeImage 图片信息
     */
    @Insert("insert into room_type_image (id, room_type_id, image_url, is_main, create_time, UPDATE_TIME) " +
            "values (ROOM_TYPE_IMAGE_SEQ.nextval, #{roomTypeId}, #{imageUrl}, #{isMain}, SYSDATE, SYSDATE)")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insertImage(RoomTypeImage roomTypeImage);
    /**
     * 根据房型ID获取图片列表
     * @param roomTypeId 房型ID
     * @return 图片列表
     */
    @Select("select * from room_type_image where room_type_id = #{roomTypeId} order by is_main desc, create_time desc")
    List<RoomTypeImage> getImagesByRoomTypeId(Long roomTypeId);
    /**
     * 根据ID获取图片信息
     * @param id 图片ID
     * @return 图片信息
     */
    @Select("select * from room_type_image where id = #{id}")
    RoomTypeImage getImageById(Long id);

    /**
     * 删除图片
     * @param id 图片ID
     */
    @Delete("delete from room_type_image where id = #{id}")
    void deleteImage(Long id);
    /**
     * 更新房型
     * @param roomType 房型信息
     */
    void update(RoomType roomType);
    /**
     * 查询指定房型下未下架的房间数量
     * @param roomTypeId 房型ID
     * @return 未下架的房间数量
     */
    @Select("select count(*) from room where room_type_id = #{roomTypeId} and status !=5")
    int countActiveRoomsByRoomTypeId(Long roomTypeId);
    /**
     * 用户端分页查询房型
     * @param roomTypeQueryDTO 查询条件
     * @return 房型列表
     */
    Page<RoomTypeVO> pageQueryForUser(RoomTypeQueryDTO roomTypeQueryDTO);
    /**
     * 更新房型权重
     * @param roomTypeId 房型ID
     * @param weight 权重值
     */
    @Update("UPDATE room_type SET weight = #{weight}, update_time = SYSDATE WHERE id = #{roomTypeId}")
    void updateWeight(@Param("roomTypeId") Long roomTypeId, @Param("weight") Integer weight);
    /**
     * 根据房型ID查询图片
     * @param roomTypeId 房型ID
     * @return 图片列表
     */
    @Select("SELECT id, image_url as imageUrl, is_main as isMain " +
            "FROM room_type_image " +
            "WHERE room_type_id = #{roomTypeId} " +
            "ORDER BY is_main DESC, id ASC")
    List<RoomTypeImageVO> getByRoomTypeId(Long roomTypeId);
    /**
     * 获取热门房型
     * @param limit 数量限制
     * @return 热门房型列表
     */
    List<HotRoomTypeVO> getHotRoomTypes(int limit);
    /**
     * 获取所有房型列表
     * @return 房型列表
     */
    List<RoomTypeNameVO> getAllRoomTypes();
}