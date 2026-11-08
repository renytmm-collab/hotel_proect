package com.example.hotel_back.Common.aspect;

import com.example.hotel_back.Common.annotation.AutoFill;
import com.example.hotel_back.Common.constant.AutoFillConstant;
import com.example.hotel_back.Common.constant.OperationTypeConstant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution(* com.example.hotel_back.mapper.*.*(..)) && @annotation(com.example.hotel_back.Common.annotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充...");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationTypeConstant operationType = autoFill.value();

        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0) return;

        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();

        try {
            // 通用时间字段设置
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);

            if(operationType == OperationTypeConstant.INSERT) {
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
            } else if(operationType == OperationTypeConstant.UPDATE) {
                setUpdateTime.invoke(entity, now);
            }
        } catch (Exception e) {
            log.error("公共字段自动填充失败：{}", e.getMessage());
        }
    }
}