package com.example.hotel_back.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class OrderUtil {
    /**
     * 生成订单号
     * @return 订单号
     */
    public static String generateOrderNo() {
        // 生成格式：年月日+6位随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Random random = new Random();
        int randomNum = random.nextInt(900000) + 100000; // 生成100000-999999之间的随机数
        return dateStr + randomNum;
    }
    public static String generatePaymentNo() {
        // 生成格式：PAY+年月日时分秒+6位随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();
        int randomNum = random.nextInt(900000) + 100000; // 生成100000-999999之间的随机数
        return "PAY" + dateStr + randomNum;
    }
    public static int newWeight(double avgScore) {
        int BaseWeight = 100;
        // 计算新的权重值
        // 权重计算公式：基础权重 + 评分调整因子 * 平均评分
        // 这里使用简单的线性关系，可以根据需求调整
        double scoreFactor = 10; // 评分调整因子
        return (int) (BaseWeight+ scoreFactor * avgScore);
    }
}
