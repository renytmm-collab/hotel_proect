package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.constant.ImcomeTypeConstant;
import com.example.hotel_back.Common.constant.OrderStatusConstant;
import com.example.hotel_back.Common.constant.RoomStatusConstant;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckInDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckInQueryDTO;
import com.example.hotel_back.Pojo.Entity.BookingOrder;
import com.example.hotel_back.Pojo.Entity.CheckIn;
import com.example.hotel_back.Pojo.Entity.IncomeRecord;
import com.example.hotel_back.Pojo.Entity.Room;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInDetailVO;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInListVO;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInResultVO;
import com.example.hotel_back.Service.CheckInService;
import com.example.hotel_back.mapper.CheckInMapper;
import com.example.hotel_back.mapper.IncomeRecordMapper;
import com.example.hotel_back.mapper.OrderMapper;
import com.example.hotel_back.mapper.RoomMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.pagehelper.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CheckInServiceImpl implements CheckInService {

    @Autowired
    private CheckInMapper checkInMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private IncomeRecordMapper incomeRecordMapper;
    @Transactional
    public CheckInResultVO checkIn(Long employeeId, CheckInDTO checkInDTO) {
        // 查询订单
        BookingOrder order = orderMapper.getById(checkInDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 检查订单状态，只有已支付待入住的订单才能办理入住
        if (order.getStatus() != OrderStatusConstant.PENDING_CHECK_IN) {
            throw new BusinessException("订单状态不正确，无法办理入住");
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(order.getCheckInDate())) {
            throw new BusinessException("尚未到预定入住日期，无法办理入住");
        }
        if (today.isAfter(order.getCheckOutDate())) {
            throw new BusinessException("已超过预定退房日期，无法办理入住");
        }
        
        // 查询房间
        Room room = roomMapper.getById(checkInDTO.getRoomId());
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        
        // 检查房间状态，只有空闲的房间才能办理入住
        if (room.getStatus() != RoomStatusConstant.FREE) { // 假设1表示空闲状态
            throw new BusinessException("房间状态不正确，无法办理入住");
        }
        
        // 检查房型是否匹配
        if (!room.getRoomTypeId().equals(order.getRoomTypeId())) {
            throw new BusinessException("房间类型与订单不匹配");
        }
//        BigDecimal depositRate = new BigDecimal("0.2");
//        BigDecimal deposit = order.getRoomPrice().multiply(new BigDecimal(order.getDays())).multiply(depositRate);
        
        // 创建入住记录
        CheckIn checkIn = new CheckIn();
        String remark = "";
        remark=checkInDTO.getRemark();
        checkIn.setOrderId(checkInDTO.getOrderId());
        checkIn.setRoomId(checkInDTO.getRoomId());
        checkIn.setGuestName(checkInDTO.getGuestName());
        checkIn.setGuestPhone(checkInDTO.getGuestPhone());
        checkIn.setGuestIdCard(checkInDTO.getGuestIdCard());
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setCheckOutDate(order.getCheckOutDate());
        checkIn.setDeposit(checkInDTO.getDeposit());
        checkIn.setStatus(0); // 在住
        checkIn.setRemark(remark);
        checkIn.setOperatorId(employeeId);
        Long userid=orderMapper.getUserIdByOrderId(checkInDTO.getOrderId());
        checkIn.setUserId(userid);
        
        // 保存入住记录
        checkInMapper.insert(checkIn);
        
        // 更新订单状态
        orderMapper.updateStatus(checkInDTO.getOrderId(), OrderStatusConstant.CHECKED_IN); // 已入住
        
        // 更新房间状态
        Room updatedRoom = new Room();
        updatedRoom.setId(checkInDTO.getRoomId());
        updatedRoom.setStatus(RoomStatusConstant.OCCUPIED);
        roomMapper.updateStatus(updatedRoom); // 已入住
        
        // 创建押金收入记录
        IncomeRecord incomeRecord = new IncomeRecord();
        incomeRecord.setOrderId(checkInDTO.getOrderId());
        incomeRecord.setAmount(checkInDTO.getDeposit());
        incomeRecord.setType(ImcomeTypeConstant.DEPOSIT); // 押金
        incomeRecord.setPaymentMethod("支付宝");
        incomeRecord.setPaymentTime(LocalDateTime.now());
        incomeRecord.setOperatorId(employeeId);
        incomeRecord.setRemark("入住押金");
        incomeRecord.setCreateTime(LocalDateTime.now());
        
        // 保存收入记录
        incomeRecordMapper.insert(incomeRecord);
        
        // 构建返回结果
        CheckInResultVO checkInResult = new CheckInResultVO();
        checkInResult.setCheckInId(checkIn.getId());
        checkInResult.setRoomNumber(room.getRoomNumber());
        
        log.info("办理入住成功，员工ID：{}，订单ID：{}，入住ID：{}", employeeId, checkInDTO.getOrderId(), checkIn.getId());
        
        return checkInResult;
    }
    public PageResult pageQuery(CheckInQueryDTO checkInQueryDTO) {
        // 开启分页
        PageHelper.startPage(checkInQueryDTO.getPageNum(), checkInQueryDTO.getPageSize());

        // 执行查询
        Page<CheckInListVO> page = checkInMapper.pageQuery(checkInQueryDTO);

        // 获取结果
        long total = page.getTotal();
        List<CheckInListVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    public CheckInDetailVO getDetail(Long id){
        // 查询入住详情
        CheckInDetailVO checkInDetail = checkInMapper.getDetail(id);

        if (checkInDetail == null) {
            throw new BusinessException("入住记录不存在");
        }

        return checkInDetail;
    }
}