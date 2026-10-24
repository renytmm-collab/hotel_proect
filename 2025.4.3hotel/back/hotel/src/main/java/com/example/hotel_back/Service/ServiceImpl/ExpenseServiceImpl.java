package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.exception.BusinessException;
import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.ExpenseAddDTO;
import com.example.hotel_back.Pojo.DAO.ExpenseQueryDTO;
import com.example.hotel_back.Pojo.DAO.ExpenseUpdateDTO;
import com.example.hotel_back.Pojo.Entity.ExpenseRecord;
import com.example.hotel_back.Pojo.VO.ExpenseRecordVO;
import com.example.hotel_back.Service.ExpenseService;
import com.example.hotel_back.mapper.ExpenseRecordMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRecordMapper expenseRecordMapper;

    @Override
    public PageResult pageQuery(ExpenseQueryDTO queryDTO) {
        // 开启分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 执行查询
        Page<ExpenseRecordVO> page = expenseRecordMapper.pageQuery(queryDTO);
        
        // 获取结果
        long total = page.getTotal();
        List<ExpenseRecordVO> records = page.getResult();
        
        // 封装结果
        return new PageResult(total, records);
    }
    public void add(Long adminId, ExpenseAddDTO expenseAddDTO) {
        // 验证金额是否合法
        if (expenseAddDTO.getAmount() == null || expenseAddDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("支出金额必须大于0");
        }

        // 验证类型是否合法
        Integer type = expenseAddDTO.getType();
        if (type == null || type < 0 || type > 4) { // 类型范围是0-4
            throw new BusinessException("支出类型不合法");
        }

        // 验证支付方式是否为空
        if (expenseAddDTO.getPaymentMethod() == null || expenseAddDTO.getPaymentMethod().isEmpty()) {
            throw new BusinessException("支付方式不能为空");
        }

        // 创建支出记录
        ExpenseRecord expenseRecord = new ExpenseRecord();
        BeanUtils.copyProperties(expenseAddDTO, expenseRecord);
        expenseRecord.setOperatorId(adminId);

        // 保存支出记录
        expenseRecordMapper.insert(expenseRecord);

        log.info("添加支出记录成功，管理员ID：{}，金额：{}，类型：{}",
                adminId, expenseAddDTO.getAmount(), expenseAddDTO.getType());
    }
    public void update(Long adminId, ExpenseUpdateDTO expenseUpdateDTO) {
        // 查询支出记录是否存在
        ExpenseRecord expenseRecord = expenseRecordMapper.getById(expenseUpdateDTO.getId());
        if (expenseRecord == null) {
            throw new BusinessException("支出记录不存在");
        }


        BeanUtils.copyProperties(expenseUpdateDTO, expenseRecord);

        // 更新支出记录
        expenseRecordMapper.update(expenseRecord);

        log.info("修改支出记录成功，管理员ID：{}，支出ID：{}", adminId, expenseUpdateDTO.getId());
    }
}