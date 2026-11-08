package com.example.hotel_back.Controller.admin;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.Entity.RoomType;
import com.example.hotel_back.Pojo.DAO.RoomTypeDTO;
import com.example.hotel_back.Pojo.DAO.RoomTypePageQueryDTO;
import com.example.hotel_back.Pojo.DAO.RoomTypeImageDTO;
import com.example.hotel_back.Pojo.Entity.RoomTypeImage;
import com.example.hotel_back.Service.RoomTypeService;
import com.example.hotel_back.Pojo.VO.uservo.RoomTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/roomType")
@Slf4j
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody RoomTypeDTO roomTypeDTO) {
        log.info("添加房型：{}", roomTypeDTO);
        roomTypeService.add(roomTypeDTO);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<PageResult> list(RoomTypePageQueryDTO roomTypePageQueryDTO) {
        log.info("房型分页查询，参数：{}", roomTypePageQueryDTO);
        PageResult pageResult = roomTypeService.pageQuery(roomTypePageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/info/{id}")
    public Result<RoomType> getInfo(@PathVariable Long id) {
        log.info("获取房型详细信息，房型ID：{}", id);

        // 参数校验
        if (id == null) {
            return Result.error("房型ID不能为空");
        }

        RoomType roomType = roomTypeService.getById(id);
        return Result.success(roomType);
    }
    @PostMapping("/uploadImage")
    public Result<String> uploadImage(@RequestBody RoomTypeImageDTO roomTypeImageDTO) {
        log.info("上传房型图片");
        // 保存图片信息
        roomTypeService.saveImage(roomTypeImageDTO);

        return Result.success();
    }
    @GetMapping("/images/{roomTypeId}")

    public Result<List<RoomTypeImage>> getImages(@PathVariable Long roomTypeId) {
        log.info("获取房型图片列表，房型ID：{}", roomTypeId);

        // 参数校验
        if (roomTypeId == null) {
            return Result.error("房型ID不能为空");
        }

        List<RoomTypeImage> images = roomTypeService.getImagesByRoomTypeId(roomTypeId);
        return Result.success(images);
    }
    @DeleteMapping("/deleteImage/{id}")
    public Result<String> deleteImage(@PathVariable Long id) {
        log.info("删除房型图片，图片ID：{}", id);

        // 参数校验
        if (id == null) {
            return Result.error("图片ID不能为空");
        }

        roomTypeService.deleteImage(id);
        return Result.success();
    }
    @PutMapping("/update")
    public Result<String> update(@RequestBody RoomTypeDTO roomTypeDTO) {
        log.info("修改房型：{}", roomTypeDTO);

//        // 参数校验
//        if (roomTypeDTO.getId() == null) {
//            return Result.error("房型ID不能为空");
//        }
//        if (roomTypeDTO.getTypeName() == null || roomTypeDTO.getTypeName().isEmpty()) {
//            return Result.error("房型名称不能为空");
//        }
//        if (roomTypeDTO.getPrice() == null || roomTypeDTO.getPrice() <= 0) {
//            return Result.error("房型价格必须大于0");
//        }

        roomTypeService.update(roomTypeDTO);
        return Result.success();
    }
}