package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.constant.RoomStatusConstant;
import com.example.hotel_back.Common.constant.StatusConstant;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.RoomAddDTO;
import com.example.hotel_back.Pojo.DAO.RoomBatchAddDTO;
import com.example.hotel_back.Pojo.DAO.RoomPageQueryDTO;
import com.example.hotel_back.Pojo.DAO.RoomStatusUpdateDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.RoomStatusQueryDTO;
import com.example.hotel_back.Pojo.Entity.Room;
import com.example.hotel_back.Pojo.Entity.RoomType;
import com.example.hotel_back.Pojo.VO.RoomVO;
import com.example.hotel_back.Pojo.VO.employeevo.RoomDetailStatusVO;
import com.example.hotel_back.Pojo.VO.employeevo.RoomStatusVO;
import com.example.hotel_back.Service.RoomService;
import com.example.hotel_back.mapper.RoomMapper;
import com.example.hotel_back.mapper.RoomTypeMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Override
    public void add(RoomAddDTO roomAddDTO) {
        // 检查房型是否存在
        RoomType roomType = roomTypeMapper.getById(roomAddDTO.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }
        
        // 检查房间号是否已存在
        Room existingRoom = roomMapper.getByRoomNumber(roomAddDTO.getRoomNumber());
        if (existingRoom != null) {
            throw new BusinessException("房间号已存在");
        }
        
        // 创建房间对象
        Room room = new Room();
        BeanUtils.copyProperties(roomAddDTO, room);
        
        // 设置默认状态为可预订
        room.setStatus(StatusConstant.ENABLE);
        
        // 保存房间
        roomMapper.insert(room);
    }
    public void updateStatus(RoomStatusUpdateDTO roomStatusUpdateDTO) {
//        // 检查房间是否存在
//        Room room = roomMapper.getById(roomStatusUpdateDTO.getId());
//        if (room == null) {
//            throw new BusinessException("房间不存在");
//        }
//
//        // 检查状态值是否合法
//        Integer status = roomStatusUpdateDTO.getStatus();
//        if (status != 0 && status != 1 && status != 2 && status != 3) {
//            throw new BusinessException("房间状态值不合法");
//        }

        // 更新房间状态
        Room updateRoom = new Room();
        BeanUtils.copyProperties(roomStatusUpdateDTO,updateRoom);

        roomMapper.updateStatus(updateRoom);
    }
    @Transactional
    public void batchAdd(RoomBatchAddDTO roomBatchAddDTO) {
        // 验证房间类型是否存在
        RoomType roomType = roomTypeMapper.getById(roomBatchAddDTO.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException("房间类型不存在");
        }

        // 获取起始房间号和数量
        String startNumber = roomBatchAddDTO.getStartNumber();
        Integer count = roomBatchAddDTO.getCount();

        // 解析起始房间号
        int startNum = Integer.parseInt(startNumber);

        // 批量添加房间
        int successCount = 0;
        int skippedCount = 0;

        for (int i = 0; i < count; i++) {
            // 生成房间号
            String roomNumber = String.valueOf(startNum + i);

            // 检查房间号是否已存在
            Room existCount = roomMapper.getByRoomNumber(roomNumber);
            if (existCount!=null) {
                log.info("房间号 {} 已存在，跳过", roomNumber);
                skippedCount++;
                continue;
            }

            // 创建房间对象
            Room room = new Room();
            room.setRoomNumber(roomNumber);
            room.setRoomTypeId(roomBatchAddDTO.getRoomTypeId());
            room.setFloor(roomBatchAddDTO.getFloor());
            room.setStatus(StatusConstant.ENABLE);

            // 单个插入房间
            roomMapper.insert(room);
            successCount++;
        }

        log.info("批量添加房间完成，成功添加 {} 个房间，跳过 {} 个已存在的房间",
                successCount, skippedCount);
    }
    @Override
    public PageResult pageQuery(RoomPageQueryDTO roomPageQueryDTO) {
        // 开启分页
        PageHelper.startPage(roomPageQueryDTO.getPageNum(), roomPageQueryDTO.getPageSize());

        // 执行查询
        Page<RoomVO> page = roomMapper.pageQuery(roomPageQueryDTO);

        // 获取结果
        long total = page.getTotal();
        List<RoomVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    public PageResult pageQueryRoomStatus(RoomStatusQueryDTO roomStatusQueryDTO) {
        // 开启分页
        PageHelper.startPage(roomStatusQueryDTO.getPageNum(), roomStatusQueryDTO.getPageSize());

        // 执行查询
        Page<RoomStatusVO> page = roomMapper.pageQueryRoomStatus(roomStatusQueryDTO);

        // 获取结果
        long total = page.getTotal();
        List<RoomStatusVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    @Override
    public RoomDetailStatusVO getRoomDetailStatusById(Long roomId) {
        // 查询房间详细状态
        RoomDetailStatusVO roomDetailStatus = roomMapper.getRoomDetailStatus(roomId);

        if (roomDetailStatus == null) {
            throw new BusinessException("房间不存在");
        }

        // 如果房间状态是已入住，查询入住信息
        if (roomDetailStatus.getStatus() == RoomStatusConstant.OCCUPIED) { // 假设3表示已入住状态
            // 查询当前入住信息
            Map<String, Object> checkInInfo = roomMapper.getCurrentCheckInInfo(roomId);

            if (checkInInfo != null) {
                // 设置入住人信息
                roomDetailStatus.setGuestName((String) checkInInfo.get("guestName"));
                roomDetailStatus.setGuestPhone((String) checkInInfo.get("guestPhone"));
                roomDetailStatus.setGuestIdCard((String) checkInInfo.get("guestIdCard"));
                roomDetailStatus.setCheckInTime((LocalDateTime) checkInInfo.get("checkInTime"));
                roomDetailStatus.setCheckOutDate((LocalDateTime) checkInInfo.get("checkOutDate"));
            }
        }

        return roomDetailStatus;
    }
}