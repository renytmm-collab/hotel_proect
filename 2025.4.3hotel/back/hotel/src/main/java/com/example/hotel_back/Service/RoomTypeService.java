package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.RoomTypeDTO;
import com.example.hotel_back.Pojo.DAO.RoomTypeImageDTO;
import com.example.hotel_back.Pojo.DAO.RoomTypePageQueryDTO;
import com.example.hotel_back.Pojo.DAO.userdao.RoomTypeQueryDTO;
import com.example.hotel_back.Pojo.Entity.RoomType;
import com.example.hotel_back.Pojo.Entity.RoomTypeImage;
import com.example.hotel_back.Pojo.VO.RoomTypeNameVO;
import com.example.hotel_back.Pojo.VO.uservo.HotRoomTypeVO;
import com.example.hotel_back.Pojo.VO.uservo.RoomTypeDetailVO;

import java.util.List;

public interface RoomTypeService {
    /**
     * 添加房型
     * @param roomTypeDTO 房型信息
     */
    void add(RoomTypeDTO roomTypeDTO);
    /**
     * 管理员分页查询房型
     * @param roomTypePageQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(RoomTypePageQueryDTO roomTypePageQueryDTO);
    /**
     * 根据ID获取房型详细信息
     * @param id 房型ID
     * @return 房型详细信息
     */
    RoomType getById(Long id);
    /**
     * 保存房型图片
     * @param roomTypeImageDTO 图片信息
     */
    void saveImage(RoomTypeImageDTO roomTypeImageDTO);
    /**
     * 根据房型ID获取图片列表
     * @param roomTypeId 房型ID
     * @return 图片列表
     */
    List<RoomTypeImage> getImagesByRoomTypeId(Long roomTypeId);
    void deleteImage(Long id);
    /**
     * 修改房型
     * @param roomTypeDTO 房型信息
     */
    void update(RoomTypeDTO roomTypeDTO);
    /**
     * 用户端分页查询房型
     * @param roomTypeQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQueryForUser(RoomTypeQueryDTO roomTypeQueryDTO);
    RoomTypeDetailVO getRoomTypeDetail(Long id);
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