package com.example.hotel_back.Controller.admin;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.RoomAddDTO;
import com.example.hotel_back.Pojo.DAO.RoomBatchAddDTO;
import com.example.hotel_back.Pojo.DAO.RoomStatusUpdateDTO;
import com.example.hotel_back.Pojo.DAO.RoomPageQueryDTO;
import com.example.hotel_back.Service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/room")
@Slf4j
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody RoomAddDTO roomAddDTO) {
        log.info("添加房间：{}", roomAddDTO);
        
//        // 参数校验
//        if (roomAddDTO.getRoomNumber() == null || roomAddDTO.getRoomNumber().isEmpty()) {
//            return Result.error("房间号不能为空");
//        }
//        if (roomAddDTO.getRoomTypeId() == null) {
//            return Result.error("房型ID不能为空");
//        }
//        if (roomAddDTO.getFloor() == null || roomAddDTO.getFloor().isEmpty()) {
//            return Result.error("楼层不能为空");
//        }
        
        roomService.add(roomAddDTO);
        return Result.success();
    }
    @PutMapping("/update")
    public Result<String> updateStatus(@RequestBody RoomStatusUpdateDTO roomStatusUpdateDTO) {
        log.info("修改房间状态：{}", roomStatusUpdateDTO);

//        // 参数校验
//        if (roomStatusUpdateDTO.getId() == null) {
//            return Result.error("房间ID不能为空");
//        }
//        if (roomStatusUpdateDTO.getStatus() == null) {
//            return Result.error("房间状态不能为空");
//        }

        roomService.updateStatus(roomStatusUpdateDTO);
        return Result.success();
    }
    @PostMapping("/batchAdd")

    public Result<String> batchAdd(@RequestBody RoomBatchAddDTO roomBatchAddDTO) {
        log.info("批量添加房间：{}", roomBatchAddDTO);
        roomService.batchAdd(roomBatchAddDTO);
        return Result.success();

    }
    @GetMapping("/list")
    public Result<PageResult> list(RoomPageQueryDTO roomPageQueryDTO) {
        log.info("房间分页查询，参数：{}", roomPageQueryDTO);
        PageResult pageResult = roomService.pageQuery(roomPageQueryDTO);
        return Result.success(pageResult);
    }

}