package com.example.hotel_back.Controller.user;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.userdao.RoomTypeQueryDTO;
import com.example.hotel_back.Pojo.VO.uservo.HotRoomTypeVO;
import com.example.hotel_back.Pojo.VO.uservo.RoomTypeDetailVO;
import com.example.hotel_back.Service.RoomTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/roomType")
@Slf4j
public class UserRoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping("/list")
    public Result<PageResult> list(RoomTypeQueryDTO roomTypeQueryDTO) {
        log.info("获取房型列表：{}", roomTypeQueryDTO);
        
        PageResult pageResult = roomTypeService.pageQueryForUser(roomTypeQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/{id}")
    public Result<RoomTypeDetailVO> getRoomTypeDetail(@PathVariable Long id) {
        log.info("获取房型详情，房型ID：{}", id);

        RoomTypeDetailVO roomTypeDetailVO = roomTypeService.getRoomTypeDetail(id);
        return Result.success(roomTypeDetailVO);
    }
    @GetMapping("/hot")
    public Result<List<HotRoomTypeVO>> getHotRoomTypes(@RequestParam(defaultValue = "6") int limit) {
        log.info("获取热门房型，数量限制：{}", limit);

        List<HotRoomTypeVO> hotRoomTypes = roomTypeService.getHotRoomTypes(limit);
        return Result.success(hotRoomTypes);
    }
}