package com.example.hotel_back.mapper;

import com.example.hotel_back.Pojo.Entity.ReviewImage;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewImageMapper {
    /**
     * 添加评价图片
     * @param reviewImage 评价图片信息
     */
    @Insert("INSERT INTO review_image (review_id, image_url, create_time) " +
            "VALUES (#{reviewId}, #{imageUrl}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(ReviewImage reviewImage);

    /**
     * 根据评价ID列表查询评价图片
     * @param reviewIds 评价ID列表
     * @return 评价图片列表
     */
    List<ReviewImage> getImagesByReviewIds(List<Long> reviewIds);

    /**
     * 根据评价ID删除评价图片
     * @param reviewId 评价ID
     */
    @Delete("DELETE FROM review_image WHERE review_id = #{reviewId}")
    void deleteByReviewId(Long reviewId);

    /**
     * 根据评论ID列表查询图片
     * @param reviewIds 评论ID列表
     * @return 图片信息列表
     */
    @MapKey("review_id")
    List<Map<String, Object>> getByReviewIds(@Param("list") List<Long> reviewIds);

    /**
     * 获取评价图片
     * @param reviewId 评价ID
     * @return 图片URL列表
     */
    @Select("SELECT image_url FROM review_image WHERE review_id = #{reviewId}")
    List<String> getImagesByReviewId(@Param("reviewId") Long reviewId);
}