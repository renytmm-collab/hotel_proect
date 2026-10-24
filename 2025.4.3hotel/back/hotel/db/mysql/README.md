# MySQL 8.4 迁移说明

状态：已完成从 Oracle 到本机 MySQL 8.4（`127.0.0.1:3306/hotel`）的代码改写、表结构校准、工作区数据导入及端到端集成测试验证。

## 1. 完成内容概览

1. **配置与驱动切换**：
   - [`pom.xml`](../../pom.xml) 切换为 `com.mysql:mysql-connector-j`。
   - [`application.yml`](../../src/main/resources/application.yml) 与 [`application-dev.yml`](../../src/main/resources/application-dev.yml) 使用独立 `MYSQL_*` 键连接 MySQL（`connectionTimeZone=Asia/Shanghai&forceConnectionTimeZoneToSession=true&characterEncoding=UTF-8`），本机私有配置写入被 `.gitignore` 忽略的 `application-local.yml`。
2. **Mapper 与 SQL 迁移**：
   - 16 个 Mapper 接口与 XML 映射文件全部移除 Oracle 序列/触发器依赖，改为 MySQL `AUTO_INCREMENT` 与 `useGeneratedKeys="true" keyProperty="id"`。
   - Oracle `ROWNUM`、`TO_DATE`/`TRUNC`、`||` 字符串拼接、`CONNECT BY` 递归日期序列已分别迁移为 MySQL `LIMIT`、`DATE()`/`DATE_ADD()`/`STR_TO_DATE()`、`CONCAT()` 与 `WITH RECURSIVE` CTE（带 `/*+ SET_VAR(cte_max_recursion_depth=100000) */`）。
   - 房间批量插入使用 `ON DUPLICATE KEY UPDATE id = room.id` 保持幂等且不覆盖已有房间状态。
   - [`RoomTypeServiceImpl.java`](../../src/main/java/com/example/hotel_back/Service/ServiceImpl/RoomTypeServiceImpl.java) 兼容不同 JDBC 驱动返回的 `Number` 实现类型。
3. **表结构校准与初始数据导入**：
   - 依据工作区内的《数据库建表语句（mysql）.docx》、《数据库结构.docx》及 `.idea/dataSources/` 缓存的库表元数据，完善 [`schema.sql`](./schema.sql) 中 16 张 InnoDB 表的字段精度（`DECIMAL(10,2)` / `DECIMAL(5,2)`）、默认值（`status`、`is_main`、`is_anonymous`、`flag`）与唯一约束（`uq_role_name`、`uq_employee_emp_no`、`uq_employee_username`、`uq_employee_phone`、`uq_users_phone`、`uq_room_type_name`、`uq_room_number`、`uq_order_no`）。
   - 整理工作区数据生成 [`seed_data.sql`](./seed_data.sql) 并导入本机 `hotel` 库（包含角色、管理员与员工、用户、5 类房型及对应图片、12 间客房、预订订单、入住记录、退房结算、评价与图片、收藏、收支流水、考勤与绩效）。

## 2. 默认测试账号（密码均为 `123456`）

| 身份 | 登录账号 | 密码 | 说明 |
|---|---|---|---|
| 管理员 | 工号 `admin` | `123456` | `role_id = 1`，后台管理端 |
| 前台员工 | 工号 `E001` | `123456` | `role_id = 2`，员工接待端 |
| 客房保洁 | 工号 `E002` | `123456` | `role_id = 3`，普通员工 |
| 用户 | 手机号 `13800138001` / `13900139001` / `13700137003` | `123456` | 客户端预订与评价 |

## 3. 验证结果

- **MyBatis SQL 合同测试**（[`MySqlMapperContractTests.java`](../../src/test/java/com/example/hotel_back/MySqlMapperContractTests.java)）：6 项全部通过。
- **Spring Boot 真实 MySQL 集成测试**（[`HotelBackApplicationTests.java`](../../src/test/java/com/example/hotel_back/HotelBackApplicationTests.java)）：5 项全部通过（覆盖上下文加载、管理员/用户登录与分页筛选、热门房型与详情、房间批量幂等插入、跨年/超千天递归 CTE 统计、预订→入住→退房完整事务回滚验证）。
- **迁移工具单元测试**（[`test_migrate.py`](./test_migrate.py)）：6 项全部通过。
