package com.example.hotel_back.Service.ServiceImpl;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.IncomeQueryDTO;
import com.example.hotel_back.Pojo.VO.IncomeRecordVO;
import com.example.hotel_back.Service.IncomeService;
import com.example.hotel_back.mapper.IncomeRecordMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class IncomeServiceImpl implements IncomeService {

    @Autowired
    private IncomeRecordMapper incomeRecordMapper;

    @Override
    public PageResult pageQuery(IncomeQueryDTO queryDTO) {
        // 开启分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 执行查询
        Page<IncomeRecordVO> page = incomeRecordMapper.pageQuery(queryDTO);
        
        // 获取结果
        long total = page.getTotal();
        List<IncomeRecordVO> records = page.getResult();
        
        // 封装结果
        return new PageResult(total, records);
    }
}