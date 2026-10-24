package com.example.hotel_back.Common.constant;

public class OrderStatusConstant {
    //订单状态（0 已取消，1 待支付，2 已支付待入住，3 已入住，4 已完成）
    public static final int CANCELED = 0;
    public static final int PENDING_PAYMENT = 1;
    public static final int PENDING_CHECK_IN = 2;
    public static final int CHECKED_IN = 3;
    public static final int COMPLETED = 4;
}
