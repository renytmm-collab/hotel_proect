package com.example.hotel_back.Controller.admin;

import com.example.hotel_back.Common.constant.MessageConstant;
import com.example.hotel_back.Common.result.Result;
import com.example.hotel_back.Pojo.VO.RoleNameVO;
import com.example.hotel_back.Pojo.VO.RoomTypeNameVO;
import com.example.hotel_back.Service.RoleService;
import com.example.hotel_back.Service.RoomTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    @Autowired
    private RoleService roleService;

    @Autowired
    private RoomTypeService roomTypeService;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-url}")
    private String accessUrl;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}", file);

        try {
            // 原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取原始文件名的后缀 dfdfdf.png
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 构造新文件名称
            String objectName = UUID.randomUUID().toString() + extension;

            // 确保上传目录存在
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 构建文件保存路径
            String filePath = uploadPath + File.separator + objectName;
            File saveFile = new File(filePath);

            // 保存文件到本地
            file.transferTo(saveFile);

            // 返回文件访问URL
            String fileUrl = accessUrl + "/" + objectName;

            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
    @GetMapping("/getrole")
    public Result<List<RoleNameVO>> getRoleList() {
        log.info("获取角色列表");

        List<RoleNameVO> roleList = roleService.getAllRoles();
        return Result.success(roleList);
    }
    @GetMapping("/getype")
    public Result<List<RoomTypeNameVO>> getRoomTypeList() {
        log.info("获取房型名列表");

        List<RoomTypeNameVO> roomTypeList = roomTypeService.getAllRoomTypes();
        return Result.success(roomTypeList);
    }
}
