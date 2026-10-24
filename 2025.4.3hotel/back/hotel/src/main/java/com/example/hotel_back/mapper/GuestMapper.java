package com.example.hotel_back.mapper;

import com.example.hotel_back.Pojo.VO.employeevo.GuestStayRecordVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GuestMapper {
    
    /**
     * 查询客人入住记录
     * @param guestIdCard 客人身份证号
     * @return 入住记录列表
     */
    Page<GuestStayRecordVO> getGuestStayRecords(@Param("guestIdCard") String guestIdCard);
}