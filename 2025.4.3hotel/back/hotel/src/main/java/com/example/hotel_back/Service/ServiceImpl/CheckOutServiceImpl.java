package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.constant.ExpenseTypeConstant;
import com.example.hotel_back.Common.constant.ImcomeTypeConstant;
import com.example.hotel_back.Common.constant.OrderStatusConstant;
import com.example.hotel_back.Common.constant.RoomStatusConstant;
import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckOutDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckOutQueryDTO;
import com.example.hotel_back.Pojo.Entity.*;
import com.example.hotel_back.Pojo.VO.employeevo.CheckOutListVO;
import com.example.hotel_back.Service.CheckOutService;
import com.example.hotel_back.mapper.CheckInMapper;
import com.example.hotel_back.mapper.CheckOutSettlementMapper;
import com.example.hotel_back.mapper.ExpenseRecordMapper;
import com.example.hotel_back.mapper.IncomeRecordMapper;
import com.example.hotel_back.mapper.OrderMapper;
import com.example.hotel_back.mapper.RoomMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CheckOutServiceImpl implements CheckOutService {

    @Autowired
    private CheckInMapper checkInMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private CheckOutSettlementMapper checkOutSettlementMapper;
    
    @Autowired
    private IncomeRecordMapper incomeRecordMapper;
    
    @Autowired
    private ExpenseRecordMapper expenseRecordMapper;

    @Override
    @Transactional
    public void handleCheckOut(Long employeeId, CheckOutDTO checkOutDTO) {
        // 查询入住记录
        CheckIn checkIn = checkInMapper.getById(checkOutDTO.getCheckInId());
        if (checkIn == null) {
            throw new BusinessException("入住记录不存在");
        }
        
        // 检查入住状态，只有在住的记录才能办理退房
        if (checkIn.getStatus() != 0) { // 假设1表示在住状态
            throw new BusinessException("入住状态不正确，无法办理退房");
        }
        
        // 查询订单
        BookingOrder order = orderMapper.getById(checkIn.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 计算额外费用和损坏赔偿费
        BigDecimal extraFee = checkOutDTO.getExtraFee();
        BigDecimal damageFee = checkOutDTO.getDamageFee();

        // 计算总费用（房费 + 额外费用 + 损坏赔偿费）
        // 房费已在预订时支付，这里只是为了记录
        BigDecimal roomFee = order.getTotalAmount();
        BigDecimal totalFee = roomFee.add(extraFee).add(damageFee);

        // 计算应退还的押金
        BigDecimal deposit = checkIn.getDeposit();
        BigDecimal additionalFees = extraFee.add(damageFee);
        BigDecimal depositRefund = BigDecimal.ZERO;

        // 如果额外费用和损坏费小于押金，则退还剩余押金
        if (additionalFees.compareTo(deposit) < 0) {
            depositRefund = deposit.subtract(additionalFees);
        }

        // 创建退房结算记录
        CheckOutSettlement settlement = new CheckOutSettlement();
        settlement.setCheckInId(checkIn.getId());
        settlement.setOrderId(checkIn.getOrderId());
        settlement.setRoomId(checkIn.getRoomId());
        settlement.setUserId(order.getUserId());
        settlement.setExtraFee(extraFee);
        settlement.setDamageFee(damageFee);
        settlement.setTotalFee(totalFee); // 总费用包含房费
        settlement.setDepositRefund(depositRefund); // 计算得出的应退还押金
        settlement.setPaymentMethod(checkOutDTO.getPaymentMethod());
        settlement.setOperatorId(employeeId);
        settlement.setRemark(checkOutDTO.getRemark());
        settlement.setCreateTime(LocalDateTime.now());
        settlement.setUpdateTime(LocalDateTime.now());

        // 保存退房结算记录
        checkOutSettlementMapper.insert(settlement);


        // 更新入住记录状态
        checkInMapper.updateStatus(checkIn.getId(), 1, LocalDateTime.now());

        // 更新订单状态
        orderMapper.updateStatus(checkIn.getOrderId(), OrderStatusConstant.COMPLETED); // 4表示已完成

        // 更新房间状态
        Room updatedRoom = new Room();
        updatedRoom.setId(checkIn.getRoomId());
        updatedRoom.setStatus(RoomStatusConstant.CLEANING);
        roomMapper.updateStatus(updatedRoom);

        // 处理额外费用和押金退还
        // 如果额外费用和损坏费大于押金，需要额外收费
        if (additionalFees.compareTo(deposit) > 0) {
            // 计算需要额外收取的费用
            BigDecimal extraCharge = additionalFees.subtract(deposit);

            // 创建收入记录
            IncomeRecord incomeRecord = new IncomeRecord();
            incomeRecord.setOrderId(checkIn.getOrderId());
            incomeRecord.setAmount(extraCharge);
            incomeRecord.setType(ImcomeTypeConstant.OTHER); // 其他收入
            incomeRecord.setPaymentMethod(checkOutDTO.getPaymentMethod());
            incomeRecord.setPaymentTime(LocalDateTime.now());
            incomeRecord.setOperatorId(employeeId);
            incomeRecord.setRemark("退房额外费用");
            incomeRecord.setCreateTime(LocalDateTime.now());

            // 保存收入记录
            incomeRecordMapper.insert(incomeRecord);

            log.info("退房额外收费，入住ID：{}，金额：{}", checkIn.getId(), extraCharge);
        }
        // 如果有押金需要退还
        else if (depositRefund.compareTo(BigDecimal.ZERO) > 0) {
            // 创建支出记录
            ExpenseRecord expenseRecord = new ExpenseRecord();
            expenseRecord.setAmount(depositRefund);
            expenseRecord.setType(ExpenseTypeConstant.REFUND); // 退款
            expenseRecord.setPaymentMethod(checkOutDTO.getPaymentMethod());
            expenseRecord.setOperatorId(employeeId);
            expenseRecord.setRemark("退房押金退还");

            // 保存支出记录
            expenseRecordMapper.insert(expenseRecord);

            log.info("退房押金退还，入住ID：{}，金额：{}", checkIn.getId(), depositRefund);
        }

        log.info("办理退房成功，员工ID：{}，入住ID：{}", employeeId, checkIn.getId());
}
    public PageResult pageQuery(CheckOutQueryDTO checkOutQueryDTO) {
        // 开启分页
        PageHelper.startPage(checkOutQueryDTO.getPageNum(), checkOutQueryDTO.getPageSize());

        // 执行查询
        Page<CheckOutListVO> page = checkOutSettlementMapper.pageQuery(checkOutQueryDTO);

        // 获取结果
        long total = page.getTotal();
        List<CheckOutListVO> records = page.getResult();

        // 封装结果
        return new PageResult(total, records);
    }
    public CheckOutListVO getDetail(Long id) {
        // 查询结算详情
        CheckOutListVO detail = checkOutSettlementMapper.getDetail(id);

        if (detail == null) {
            throw new BusinessException("结算记录不存在");
        }

        return detail;
    }
}