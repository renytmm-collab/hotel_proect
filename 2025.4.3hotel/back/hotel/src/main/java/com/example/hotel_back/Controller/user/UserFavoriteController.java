package com.example.hotel_back.Controller.user;

import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.DAO.userdao.FavoriteDTO;
import com.example.hotel_back.Service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/favorite")
@Slf4j
public class UserFavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @PostMapping("/add")
    public Result<String> add(@RequestBody FavoriteDTO favoriteDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("添加收藏，用户ID：{}，房型ID：{}", userId, favoriteDTO.getRoomTypeId());
        // 参数校验
        if (favoriteDTO.getRoomTypeId() == null) {
            return Result.error("房型ID不能为空");
        }
        
        favoriteService.addFavorite(userId, favoriteDTO);
        return Result.success();
    }
    @PostMapping("/cancel")
    public Result<String> cancel(@RequestBody FavoriteDTO favoriteDTO) {
        // 从JWT中获取当前登录用户ID
        Long userId =BaseContext.getCurrentId();
        log.info("取消收藏，用户ID：{}，房型ID：{}", userId, favoriteDTO.getRoomTypeId());

        // 参数校验
        if (favoriteDTO.getRoomTypeId() == null) {
            return Result.error("房型ID不能为空");
        }

        favoriteService.cancelFavorite(userId, favoriteDTO);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<PageResult> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId =BaseContext.getCurrentId();
        log.info("获取收藏列表，用户ID：{}，页码：{}，每页记录数：{}", userId, pageNum, pageSize);

        PageResult pageResult = favoriteService.pageQuery(userId, pageNum, pageSize);
        return Result.success(pageResult);
    }
}