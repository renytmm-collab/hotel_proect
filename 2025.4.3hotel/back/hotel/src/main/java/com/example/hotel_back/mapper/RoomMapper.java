package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.RoomPageQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.RoomStatusQueryDTO;
import com.example.hotel_back.Pojo.Entity.Room;
import com.example.hotel_back.Pojo.VO.RoomVO;
import com.example.hotel_back.Pojo.VO.employeevo.RoomDetailStatusVO;
import com.example.hotel_back.Pojo.VO.employeevo.RoomStatusVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoomMapper {
    /**
     * 根据房间号查询房间
     * @param roomNumber 房间号
     * @return 房间信息
     */
    @Select("select * from room where room_number = #{roomNumber}")
    Room getByRoomNumber(String roomNumber);
    
    /**
     * 添加房间
     * @param room 房间信息
     */
    @Insert("insert into room (id, room_number, room_type_id, floor, status, create_time, update_time) " +
            "values (room_seq.nextval, #{roomNumber}, #{roomTypeId}, #{floor}, #{status}, SYSDATE, SYSDATE)")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insert(Room room);
    /**
     * 根据ID查询房间
     * @param id 房间ID
     * @return 房间信息
     */
    @Select("select * from room where id = #{id}")
    Room getById(Long id);

    /**
     * 更新房间状态
     * @param room 房间信息
     */
    @Update("update room set status = #{status}, update_time = SYSDATE where id = #{id}")
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updateStatus(Room room);
    /**
     * 批量插入房间
     * @param roomList 房间列表
     */
    void batchInsert(List<Room> roomList);
    /**
     * 分页查询房间
     * @param roomPageQueryDTO 查询条件
     * @return 房间列表
     */
    Page<RoomVO> pageQuery(RoomPageQueryDTO roomPageQueryDTO);
    /**
     * 统计指定房型的房间数量
     * @param roomTypeId 房型ID
     * @return 房间数量
     */
    @Select("select count(*) from room where room_type_id = #{roomTypeId} and status = 1")
    int countRoomsByRoomTypeId(Long roomTypeId);
    /**
     * 分页查询房态
     * @param roomStatusQueryDTO 查询条件
     * @return 房态列表
     */
    Page<RoomStatusVO> pageQueryRoomStatus(RoomStatusQueryDTO roomStatusQueryDTO);
    /**
     * 获取房间详细状态
     * @param roomId 房间ID
     * @return 房间详细状态
     */
    RoomDetailStatusVO getRoomDetailStatus(Long roomId);

    /**
     * 获取当前入住信息
     * @param roomId 房间ID
     * @return 入住信息
     */
    @MapKey("checkInTime")
    Map<String, Object> getCurrentCheckInInfo(Long roomId);



}