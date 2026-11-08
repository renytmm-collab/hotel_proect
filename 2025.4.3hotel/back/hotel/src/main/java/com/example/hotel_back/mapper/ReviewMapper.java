package com.example.hotel_back.mapper;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import com.example.hotel_back.Pojo.DAO.employeedao.ReviewQueryDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.ReviewReplyDTO;
import com.example.hotel_back.Pojo.Entity.Review;
import com.example.hotel_back.Pojo.VO.uservo.ReviewListVO;
import com.example.hotel_back.Pojo.VO.uservo.ReviewVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReviewMapper {
    /**
     * 添加评价
     * @param review 评价信息
     */
    @Insert("INSERT INTO review (user_id, order_id, room_type_id, content, score, is_anonymous, status, create_time, update_time) " +
            "VALUES (#{userId}, #{orderId}, #{roomTypeId}, #{content}, #{score}, #{isAnonymous}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @AutoFill(value = OperationTypeConstant.INSERT)
    void insert(Review review);

    /**
     * 检查订单是否已评价
     * @param orderId 订单ID
     * @return 是否已评价
     */
    @Select("SELECT COUNT(*) FROM review WHERE order_id = #{orderId}")
    boolean existsByOrderId(Long orderId);

    /**
     * 获取房型的平均评分
     * @param roomTypeId 房型ID
     * @return 平均评分
     */
    @Select("SELECT AVG(score) FROM review WHERE room_type_id = #{roomTypeId} AND status = 1")
    Double getAvgScoreByRoomTypeId(Long roomTypeId);

    /**
     * 分页查询用户评价
     * @param userId 用户ID
     * @return 评价列表
     */
    Page<ReviewListVO> pageQueryByUserId(Long userId);

    /**
     * 根据ID查询评价
     * @param id 评价ID
     * @return 评价信息
     */
    @Select("SELECT * FROM review WHERE id = #{id}")
    Review getById(Long id);

    /**
     * 根据ID删除评价
     * @param id 评价ID
     */
    @Delete("DELETE FROM review WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 根据房型ID查询评论
     * @param roomTypeId 房型ID
     * @return 评论列表
     */
    List<ReviewVO> getByRoomTypeId(Long roomTypeId);

    /**
     * 更新评价回复
     * @param review 评价信息
     */
    @AutoFill(value = OperationTypeConstant.UPDATE)
    void updateReply(Review review);

    /**
     * 分页查询评价
     * @param queryDTO 查询条件
     * @return 评价列表
     */
    Page<ReviewVO> pageQuery(ReviewQueryDTO queryDTO);

    /**
     * 根据订单ID获取评价ID
     * @param orderId 订单ID
     * @return 评价ID
     */
    @Select("SELECT id FROM review WHERE order_id = #{orderId}")
    Long getIdByOrderId(Long orderId);
}