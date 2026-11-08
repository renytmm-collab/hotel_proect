package com.example.hotel_back.Service;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.RoomAddDTO;
import com.example.hotel_back.Pojo.DAO.RoomPageQueryDTO;
import com.example.hotel_back.Pojo.DAO.RoomStatusUpdateDTO;
import com.example.hotel_back.Pojo.DAO.RoomBatchAddDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.RoomStatusQueryDTO;
import com.example.hotel_back.Pojo.VO.employeevo.RoomDetailStatusVO;

public interface RoomService {
    /**
     * 添加房间
     * @param roomAddDTO 房间信息
     */
    void add(RoomAddDTO roomAddDTO);
    /**
     * 修改房间状态
     * @param roomStatusUpdateDTO 房间状态信息
     */
    void updateStatus(RoomStatusUpdateDTO roomStatusUpdateDTO);
    /**
     * 批量添加房间
     * @param roomBatchAddDTO 批量添加房间信息
     */
    void batchAdd(RoomBatchAddDTO roomBatchAddDTO);
    /**
     * 分页查询房间
     * @param roomPageQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(RoomPageQueryDTO roomPageQueryDTO);

    /**
     * 分页查询房态
     * @param roomStatusQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQueryRoomStatus(RoomStatusQueryDTO roomStatusQueryDTO);
    /**
     * 根据ID获取房态详细信息
     * @param roomId 房间ID
     * @return 房态详细信息
     */
    RoomDetailStatusVO getRoomDetailStatusById(Long roomId);

}