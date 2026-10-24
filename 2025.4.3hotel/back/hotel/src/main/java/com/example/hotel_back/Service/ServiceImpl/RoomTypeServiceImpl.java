package com.example.hotel_back.Service.ServiceImpl;
import com.example.hotel_back.Common.constant.StatusConstant;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Pojo.DAO.RoomTypeDTO;
import com.example.hotel_back.Pojo.DAO.RoomTypeImageDTO;
import com.example.hotel_back.Pojo.DAO.RoomTypePageQueryDTO;
import com.example.hotel_back.Pojo.DAO.userdao.RoomTypeQueryDTO;
import com.example.hotel_back.Pojo.Entity.RoomType;
import com.example.hotel_back.Pojo.Entity.RoomTypeImage;
import com.example.hotel_back.Pojo.VO.RoomTypeNameVO;
import com.example.hotel_back.Pojo.VO.uservo.*;
import com.example.hotel_back.Service.RoomTypeService;
import com.example.hotel_back.mapper.ReviewImageMapper;
import com.example.hotel_back.mapper.ReviewMapper;
import com.example.hotel_back.mapper.RoomTypeMapper;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.mapper.UserMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomTypeServiceImpl implements RoomTypeService {

    @Autowired
    private RoomTypeMapper roomTypeMapper;
    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ReviewImageMapper reviewImageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void add(RoomTypeDTO roomTypeDTO) {
        // 检查房型名称是否已存在
        RoomType existingRoomType = roomTypeMapper.getByTypeName(roomTypeDTO.getTypeName());
        if (existingRoomType != null) {
            throw new BusinessException("房型名称已存在");
        }

        // 创建房型对象
        RoomType roomType = new RoomType();
        BeanUtils.copyProperties(roomTypeDTO, roomType);

        roomType.setStatus(StatusConstant.ENABLE);
        if (roomType.getWeight() == null) {
            roomType.setWeight(100); // 默认权重
        }

        // 保存房型
        roomTypeMapper.insert(roomType);
    }
    public PageResult pageQuery(RoomTypePageQueryDTO roomTypePageQueryDTO) {
        // 开启分页
        PageHelper.startPage(roomTypePageQueryDTO.getPageNum(), roomTypePageQueryDTO.getPageSize());

        // 执行查询
        Page<RoomType> page = roomTypeMapper.pageQuery(roomTypePageQueryDTO);

        // 获取结果
        long total = page.getTotal();
        List<RoomType> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    public RoomType getById(Long id) {
        // 查询房型信息
        RoomType roomType = roomTypeMapper.getById(id);

        return roomType;
    }
    @Transactional
    public void saveImage(RoomTypeImageDTO roomTypeImageDTO) {

        // 如果是主图，需要将其他主图设置为非主图
        if (roomTypeImageDTO.getIsMain() == 1) {
            roomTypeMapper.updateMainImage(roomTypeImageDTO.getRoomTypeId());
        }

        // 创建图片对象
        RoomTypeImage roomTypeImage = new RoomTypeImage();
        roomTypeImage.setRoomTypeId(roomTypeImageDTO.getRoomTypeId());
        roomTypeImage.setImageUrl(roomTypeImageDTO.getImageUrl());
        roomTypeImage.setIsMain(roomTypeImageDTO.getIsMain());

        // 保存图片信息
        roomTypeMapper.insertImage(roomTypeImage);

        }
    public List<RoomTypeImage> getImagesByRoomTypeId(Long roomTypeId) {

        // 查询房型图片列表
        return roomTypeMapper.getImagesByRoomTypeId(roomTypeId);
    }
    public void deleteImage(Long id) {
        // 查询图片信息
        RoomTypeImage image = roomTypeMapper.getImageById(id);
        if (image == null) {
            throw new BusinessException("图片不存在");
        }

        // 如果是主图，不允许删除
        if (image.getIsMain() == 1) {
            throw new BusinessException("主图不能删除，请先设置其他图片为主图");
        }

        // 删除图片
        roomTypeMapper.deleteImage(id);
    }
    @Transactional
    public void update(RoomTypeDTO roomTypeDTO) {
        // 检查房型是否存在
        RoomType existingRoomType = roomTypeMapper.getById(roomTypeDTO.getId());
        if (existingRoomType == null) {
            throw new BusinessException("房型不存在");
        }

        // 检查房型名称是否重复（排除自身）
        RoomType roomTypeWithSameName = roomTypeMapper.getByTypeName(roomTypeDTO.getTypeName());
        if (roomTypeWithSameName != null && !roomTypeWithSameName.getId().equals(roomTypeDTO.getId())) {
            throw new BusinessException("房型名称已存在");
        }

        // 如果要下架房型（状态从1改为0），需要检查是否有未下架的房间
        if (existingRoomType.getStatus() == 1 && roomTypeDTO.getStatus() == 0) {
            // 查询该房型下未下架的房间数量
            int activeRoomCount = roomTypeMapper.countActiveRoomsByRoomTypeId(roomTypeDTO.getId());
            if (activeRoomCount > 0) {
                throw new BusinessException("该房型下有" + activeRoomCount + "个未下架的房间，请先下架房间后再下架房型");
            }
        }

        // 创建房型对象
        RoomType roomType = new RoomType();
        BeanUtils.copyProperties(roomTypeDTO, roomType);

        // 更新房型
        roomTypeMapper.update(roomType);
    }
    public PageResult pageQueryForUser(RoomTypeQueryDTO roomTypeQueryDTO) {
        // 开启分页
        PageHelper.startPage(roomTypeQueryDTO.getPageNum(), roomTypeQueryDTO.getPageSize());

        // 执行查询
        Page<RoomTypeVO> page = roomTypeMapper.pageQueryForUser(roomTypeQueryDTO);

        // 获取结果
        long total = page.getTotal();
        List<RoomTypeVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    // ... existing code ...

    public RoomTypeDetailVO getRoomTypeDetail(Long id) {
        // 查询房型基本信息
        RoomType roomType = roomTypeMapper.getById(id);
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }

        // 创建返回对象
        RoomTypeDetailVO roomTypeDetailVO = new RoomTypeDetailVO();
        BeanUtils.copyProperties(roomType, roomTypeDetailVO);

        // 查询房型图片
        List<RoomTypeImageVO> images = roomTypeMapper.getByRoomTypeId(id);
        roomTypeDetailVO.setImages(images);

        // 查询房型评论
        List<ReviewVO> reviews = reviewMapper.getByRoomTypeId(id);

        if (!reviews.isEmpty()) {
            // 获取评论ID列表
            List<Long> reviewIds = reviews.stream()
                    .map(ReviewVO::getId)
                    .collect(Collectors.toList());

            // 查询评论图片
            List<Map<String, Object>> reviewImagesList = reviewImageMapper.getByReviewIds(reviewIds);
            Map<Long, List<String>> reviewImagesMap = new HashMap<>();

            // 手动处理图片映射，避免空键和类型转换问题
            for (Map<String, Object> imageMap : reviewImagesList) {
                // 处理Oracle返回的BigDecimal转为Long
                Long reviewId = null;
                if (imageMap.get("reviewId") instanceof BigDecimal) {
                    reviewId = ((BigDecimal) imageMap.get("reviewId")).longValue();
                } else if (imageMap.get("reviewId") instanceof Long) {
                    reviewId = (Long) imageMap.get("reviewId");
                }

                String imageUrl = (String) imageMap.get("imageUrl");

                if (reviewId != null && imageUrl != null) {
                    if (!reviewImagesMap.containsKey(reviewId)) {
                        reviewImagesMap.put(reviewId, new ArrayList<>());
                    }
                    reviewImagesMap.get(reviewId).add(imageUrl);
                }
            }

            List<Long> userIds = reviews.stream()
                    .filter(review -> review.getIsAnonymous() == 0) // 只查询非匿名评论的用户
                    .map(ReviewVO::getUserId)
                    .filter(Objects::nonNull) // 过滤掉null值
                    .collect(Collectors.toList());

            // 查询用户信息
            Map<Long, Map<String, Object>> userInfoMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<Map<String, Object>> userInfoList = userMapper.getByUserIds(userIds);
                for (Map<String, Object> userInfo : userInfoList) {
                    // 处理Oracle返回的BigDecimal转为Long
                    Long userId = null;
                    if (userInfo.get("id") instanceof BigDecimal) {
                        userId = ((BigDecimal) userInfo.get("id")).longValue();
                    } else if (userInfo.get("id") instanceof Long) {
                        userId = (Long) userInfo.get("id");
                    }

                    if (userId != null) {
                        userInfoMap.put(userId, userInfo);
                    }
                }
            }

            // 设置评论图片和用户信息
            for (ReviewVO review : reviews) {
                // 设置评论图片
                List<String> reviewImages = reviewImagesMap.getOrDefault(review.getId(), Collections.emptyList());
                review.setReviewImages(reviewImages);

                // 设置用户信息
                if (review.getIsAnonymous() == 0 && review.getUserId() != null) {
                    Map<String, Object> userInfo = userInfoMap.get(review.getUserId());
                    if (userInfo != null) {
                        review.setUsername((String) userInfo.get("username"));
                        review.setUserAvatar((String) userInfo.get("avatar"));
                    } else {
                        // 用户信息不存在，设置默认值
                        review.setUsername("未知用户");
                        review.setUserAvatar("");
                    }
                } else {
                    // 匿名评论
                    review.setUsername("匿名用户");
                    review.setUserAvatar("");
                }
            }
        }

        roomTypeDetailVO.setReviews(reviews);
        return roomTypeDetailVO;
    }

    // ... existing code ...
    private List<HotRoomTypeVO> hotRoomTypesCache;
    private long lastUpdateTime = 0;
    private static final long CACHE_DURATION = 60 * 1000; // 缓存时间1分钟

    public List<HotRoomTypeVO> getHotRoomTypes(int limit) {
        // 检查缓存是否过期
        long currentTime = System.currentTimeMillis();
        if (hotRoomTypesCache == null || currentTime - lastUpdateTime > CACHE_DURATION) {
            // 缓存过期，重新查询
            log.info("热门房型缓存过期，重新查询");
            hotRoomTypesCache = roomTypeMapper.getHotRoomTypes(limit);
            lastUpdateTime = currentTime;
        }

        return hotRoomTypesCache;
    }
    public List<RoomTypeNameVO> getAllRoomTypes() {
        // 查询所有房型
        List<RoomTypeNameVO> roomTypeList = roomTypeMapper.getAllRoomTypes();
        log.info("查询所有房型，共{}条记录", roomTypeList.size());
        return roomTypeList;
    }
}