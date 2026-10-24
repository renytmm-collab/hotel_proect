-- Verified against workspace documents (数据库建表语句（mysql）.docx, 数据库结构.docx)
-- and IntelliJ cached Oracle ZLW / MySQL hotel schema metadata (.idea/dataSources/).
-- Select the target database explicitly before executing.
SET NAMES utf8mb4;

CREATE TABLE `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NULL COMMENT '角色名称（唯一）',
    `description` VARCHAR(200) NULL COMMENT '角色描述',
    `status` TINYINT(1) NULL DEFAULT 1 COMMENT '状态（0=禁用，1=启用）',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `employee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '员工ID',
    `emp_no` VARCHAR(20) NULL COMMENT '工号（唯一）',
    `username` VARCHAR(50) NULL COMMENT '用户名（唯一）',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) NULL COMMENT '手机号（唯一）',
    `avatar` VARCHAR(255) NULL COMMENT '头像URL',
    `role_id` BIGINT NULL COMMENT '角色ID（逻辑关联role.id）',
    `status` TINYINT(1) NULL DEFAULT 1 COMMENT '状态（0=禁用，1=启用）',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `sex` VARCHAR(20) NULL COMMENT '性别',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_employee_emp_no` (`emp_no`),
    UNIQUE KEY `uq_employee_username` (`username`),
    UNIQUE KEY `uq_employee_phone` (`phone`),
    KEY `ix_employee_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `real_name` VARCHAR(50) NULL COMMENT '真实姓名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) NULL COMMENT '手机号（唯一）',
    `username` VARCHAR(50) NULL COMMENT '昵称',
    `avatar` VARCHAR(255) NULL COMMENT '头像URL',
    `status` TINYINT(1) NULL DEFAULT 1 COMMENT '状态（0=禁用，1=启用）',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `sex` VARCHAR(20) NULL COMMENT '性别',
    `id_card` VARCHAR(20) NULL COMMENT '身份证号',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_users_phone` (`phone`),
    KEY `ix_users_real_name` (`real_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `room_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '房型ID',
    `type_name` VARCHAR(50) NULL COMMENT '房型名称（唯一）',
    `price` DECIMAL(10,2) NULL COMMENT '标准价格',
    `area` INT NULL COMMENT '面积（平米）',
    `bed_type` VARCHAR(50) NULL COMMENT '床型',
    `max_guests` INT NULL COMMENT '最大入住人数',
    `description` TEXT NULL COMMENT '房型描述',
    `facilities` VARCHAR(500) NULL COMMENT '配套设施（逗号分隔）',
    `weight` INT NULL COMMENT '排序权重',
    `status` TINYINT(1) NULL DEFAULT 1 COMMENT '状态（0=下架，1=上架）',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_room_type_name` (`type_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `room` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '房间ID',
    `room_number` VARCHAR(20) NOT NULL COMMENT '房间号（唯一）',
    `room_type_id` BIGINT NULL COMMENT '房型ID（逻辑关联room_type.id）',
    `floor` VARCHAR(10) NULL COMMENT '楼层',
    `status` TINYINT(1) NULL DEFAULT 0 COMMENT '状态（1=空闲，2=已入住，3=清洁中，4=维修中，5=下架）',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_room_number` (`room_number`),
    KEY `ix_room_type` (`room_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `room_type_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `room_type_id` BIGINT NULL COMMENT '房型ID（逻辑关联room_type.id）',
    `image_url` VARCHAR(255) NULL COMMENT '图片URL',
    `is_main` TINYINT(1) NULL DEFAULT 0 COMMENT '是否主图（0=否，1=是）',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    `update_time` DATETIME NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `ix_room_image` (`room_type_id`, `is_main`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `booking_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NULL COMMENT '订单编号（唯一）',
    `user_id` BIGINT NULL COMMENT '用户ID（逻辑关联users.id）',
    `room_type_id` BIGINT NULL COMMENT '房型ID（逻辑关联room_type.id）',
    `room_price` DECIMAL(10,2) NULL COMMENT '房间单价',
    `check_in_date` DATE NULL COMMENT '入住日期',
    `check_out_date` DATE NULL COMMENT '退房日期',
    `days` INT NULL COMMENT '入住天数',
    `guest_name` VARCHAR(50) NULL COMMENT '入住人姓名',
    `guest_phone` VARCHAR(20) NULL COMMENT '入住人电话',
    `guest_id_card` VARCHAR(20) NULL COMMENT '入住人身份证',
    `total_amount` DECIMAL(10,2) NULL COMMENT '订单总金额',
    `status` TINYINT(1) NULL DEFAULT 1 COMMENT '订单状态（0=已取消，1=待支付，2=已支付待入住，3=已入住，4=已完成）',
    `payment_method` VARCHAR(20) NULL COMMENT '支付方式',
    `payment_time` DATETIME NULL COMMENT '支付时间',
    `payment_no` VARCHAR(100) NULL COMMENT '支付流水号',
    `cancel_reason` VARCHAR(200) NULL COMMENT '取消原因',
    `cancel_time` DATETIME NULL COMMENT '取消时间',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_order_no` (`order_no`),
    KEY `ix_order_user` (`user_id`),
    KEY `ix_order_dates` (`room_type_id`, `status`, `check_in_date`, `check_out_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `check_in_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `order_id` BIGINT NULL COMMENT '订单ID（逻辑关联booking_order.id）',
    `room_id` BIGINT NULL COMMENT '房间ID（逻辑关联room.id）',
    `user_id` BIGINT NULL COMMENT '用户ID（逻辑关联users.id）',
    `guest_name` VARCHAR(50) NULL COMMENT '入住人姓名',
    `guest_phone` VARCHAR(20) NULL COMMENT '入住人电话',
    `guest_id_card` VARCHAR(20) NULL COMMENT '入住人身份证',
    `check_in_time` DATETIME NULL COMMENT '实际入住时间',
    `deposit` DECIMAL(10,2) NULL COMMENT '押金金额',
    `status` TINYINT(1) NULL DEFAULT 0 COMMENT '状态（0=入住中，1=已退房）',
    `operator_id` BIGINT NULL COMMENT '操作员工ID（逻辑关联employee.id）',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `check_out_time` DATETIME NULL COMMENT '实际退房时间',
    `check_out_date` DATE NULL COMMENT '预计退房日期',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `ix_checkin_room` (`room_id`, `status`, `check_in_time`),
    KEY `ix_checkin_order` (`order_id`),
    KEY `ix_checkin_guest` (`guest_id_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `check_out_settlement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '结算ID',
    `check_in_id` BIGINT NULL COMMENT '入住记录ID（逻辑关联check_in_record.id）',
    `order_id` BIGINT NULL COMMENT '订单ID（逻辑关联booking_order.id）',
    `room_id` BIGINT NULL COMMENT '房间ID（逻辑关联room.id）',
    `user_id` BIGINT NULL COMMENT '用户ID（逻辑关联users.id）',
    `extra_fee` DECIMAL(10,2) NULL COMMENT '额外消费金额',
    `damage_fee` DECIMAL(10,2) NULL COMMENT '损坏赔偿金额',
    `total_fee` DECIMAL(10,2) NULL COMMENT '结算总费用',
    `deposit_refund` DECIMAL(10,2) NULL COMMENT '押金退还金额',
    `payment_method` VARCHAR(20) NULL COMMENT '支付方式',
    `operator_id` BIGINT NULL COMMENT '操作员工ID（逻辑关联employee.id）',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `ix_settlement_checkin` (`check_in_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `attendance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `employee_id` BIGINT NULL COMMENT '员工ID（逻辑关联employee.id）',
    `attendance_date` DATE NULL COMMENT '考勤日期',
    `check_in_time` DATETIME NULL COMMENT '签到时间',
    `check_out_time` DATETIME NULL COMMENT '签退时间',
    `status` TINYINT(1) NULL COMMENT '状态（0=正常，1=迟到，2=早退，3=旷工，4=请假，5=已签到未签退）',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `ix_attendance_employee_date` (`employee_id`, `attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `performance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `employee_id` BIGINT NULL COMMENT '员工ID（逻辑关联employee.id）',
    `month` VARCHAR(7) NULL COMMENT '考核月份（yyyy-MM）',
    `score` DECIMAL(5,2) NULL COMMENT '绩效评分',
    `evaluation` VARCHAR(500) NULL COMMENT '考评评语',
    `evaluator_id` BIGINT NULL COMMENT '考评人ID（逻辑关联employee.id）',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `ix_performance_employee_month` (`employee_id`, `month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `user_id` BIGINT NULL COMMENT '用户ID（逻辑关联users.id）',
    `order_id` BIGINT NULL COMMENT '订单ID（逻辑关联booking_order.id）',
    `room_type_id` BIGINT NULL COMMENT '房型ID（逻辑关联room_type.id）',
    `content` VARCHAR(500) NULL COMMENT '评价内容',
    `score` TINYINT(1) NULL COMMENT '评分（1-5）',
    `is_anonymous` TINYINT(1) NULL DEFAULT 0 COMMENT '是否匿名（0=否，1=是）',
    `status` TINYINT(1) NULL DEFAULT 1 COMMENT '状态（0=隐藏，1=显示）',
    `reply` VARCHAR(500) NULL COMMENT '酒店回复内容',
    `reply_time` DATETIME NULL COMMENT '回复时间',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `ix_review_room_type` (`room_type_id`, `status`),
    KEY `ix_review_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `review_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `review_id` BIGINT NULL COMMENT '评价ID（逻辑关联review.id）',
    `image_url` VARCHAR(255) NULL COMMENT '图片URL',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `ix_review_image` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NULL COMMENT '用户ID（逻辑关联users.id）',
    `room_type_id` BIGINT NULL COMMENT '房型ID（逻辑关联room_type.id）',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `ix_favorite_user_room_type` (`user_id`, `room_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `income_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `order_id` BIGINT NULL COMMENT '订单ID（逻辑关联booking_order.id）',
    `amount` DECIMAL(10,2) NULL COMMENT '收入金额',
    `type` TINYINT(1) NULL COMMENT '收入类型（0=房费，1=押金，2=额外消费）',
    `payment_method` VARCHAR(20) NULL COMMENT '支付方式',
    `payment_time` DATETIME NULL COMMENT '支付时间',
    `operator_id` BIGINT NULL COMMENT '操作员工ID（逻辑关联employee.id）',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `ix_income_payment_time` (`payment_time`),
    KEY `ix_income_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE `expense_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `amount` DECIMAL(10,2) NULL COMMENT '支出金额',
    `type` TINYINT(1) NULL COMMENT '支出类型（0=退款，1=采购，2=工资，3=水电，4=其他）',
    `payment_method` VARCHAR(20) NULL COMMENT '支付方式',
    `operator_id` BIGINT NULL COMMENT '操作员工ID（逻辑关联employee.id）',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `create_time` DATETIME NULL COMMENT '创建时间',
    `update_time` DATETIME NULL COMMENT '更新时间',
    `flag` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `ix_expense_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;
