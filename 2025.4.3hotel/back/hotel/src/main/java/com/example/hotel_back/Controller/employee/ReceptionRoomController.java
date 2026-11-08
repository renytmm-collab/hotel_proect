package com.example.hotel_back.Controller.employee;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.employeedao.RoomStatusQueryDTO;
import com.example.hotel_back.Pojo.DAO.RoomStatusUpdateDTO;
import com.example.hotel_back.Pojo.VO.employeevo.RoomDetailStatusVO;
import com.example.hotel_back.Service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/reception/room")
@Slf4j

public class ReceptionRoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/status")
    public Result<PageResult> getRoomStatusList(RoomStatusQueryDTO roomStatusQueryDTO) {
        log.info("获取房态列表，查询条件：{}", roomStatusQueryDTO);
        
        PageResult pageResult = roomService.pageQueryRoomStatus(roomStatusQueryDTO);
        return Result.success(pageResult);
    }
    @PutMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestBody RoomStatusUpdateDTO roomStatusUpdateDTO) {
        log.info("更新房间状态，房间ID：{}，状态：{}", roomStatusUpdateDTO.getId(), roomStatusUpdateDTO.getStatus());

        roomService.updateStatus(roomStatusUpdateDTO);
        return Result.success();
    }
    @GetMapping("/detailStatus/{id}")
    public Result<RoomDetailStatusVO> getRoomDetailStatusById(@PathVariable Long id) {
        log.info("获取房态具体信息，房间ID：{}", id);

        RoomDetailStatusVO roomDetailStatus = roomService.getRoomDetailStatusById(id);
        return Result.success(roomDetailStatus);
    }
}