package com.example.hotel_back;

import com.example.hotel_back.Common.result.PageResult;
import com.example.hotel_back.Pojo.DAO.*;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckInDTO;
import com.example.hotel_back.Pojo.DAO.employeedao.CheckOutDTO;
import com.example.hotel_back.Pojo.DAO.userdao.OrderQueryDTO;
import com.example.hotel_back.Pojo.DAO.userdao.RoomTypeQueryDTO;
import com.example.hotel_back.Pojo.DAO.userdao.UserLoginDTO;
import com.example.hotel_back.Pojo.Entity.BookingOrder;
import com.example.hotel_back.Pojo.Entity.Employee;
import com.example.hotel_back.Pojo.Entity.Room;
import com.example.hotel_back.Pojo.Entity.User;
import com.example.hotel_back.Pojo.VO.employeevo.CheckInResultVO;
import com.example.hotel_back.Pojo.VO.uservo.HotRoomTypeVO;
import com.example.hotel_back.Pojo.VO.uservo.RoomTypeDetailVO;
import com.example.hotel_back.Service.*;
import com.example.hotel_back.mapper.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class HotelBackApplicationTests {

    @Autowired
    private AdminService adminService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoomTypeService roomTypeService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private CheckInService checkInService;
    @Autowired
    private CheckOutService checkOutService;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StatsMapper statsMapper;
    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private PerformanceMapper performanceMapper;
    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private FavoriteMapper favoriteMapper;

    @Test
    void contextLoads() {
        assertNotNull(adminService);
    }

    @Test
    void loginAndUserEmployeeQueriesWorkAgainstMySql() {
        EmployeeLoginDTO empLogin = new EmployeeLoginDTO();
        empLogin.setEmpNo("admin");
        empLogin.setPassword("123456");
        Employee admin = adminService.login(empLogin);
        assertEquals(1L, admin.getRoleId());

        EmployeePageQueryDTO empQuery = new EmployeePageQueryDTO();
        empQuery.setPageNum(1);
        empQuery.setPageSize(10);
        empQuery.setBeginTime(LocalDateTime.of(2025, 4, 1, 0, 0));
        empQuery.setEndTime(LocalDateTime.of(2025, 4, 30, 23, 59));
        PageResult empPage = adminService.employeePageQuery(empQuery);
        assertTrue(empPage.getTotal() >= 2);

        UserLoginDTO userLogin = new UserLoginDTO();
        userLogin.setPhone("13800138001");
        userLogin.setPassword("123456");
        User user = userService.login(userLogin);
        assertEquals("张三丰", user.getRealName());

        UserPageQueryDTO userQuery = new UserPageQueryDTO();
        userQuery.setPageNum(1);
        userQuery.setPageSize(10);
        userQuery.setRealName("张");
        PageResult userPage = userService.pageQuery(userQuery);
        assertEquals(1L, userPage.getTotal());
    }

    @Test
    void roomTypeDetailHotRoomsAndRoomBatchInsertWork() {
        List<HotRoomTypeVO> hot = roomTypeService.getHotRoomTypes(5);
        assertFalse(hot.isEmpty());
        assertEquals("行政套房", hot.get(0).getTypeName());
        assertNotNull(hot.get(0).getMainImage());

        RoomTypeDetailVO detail = roomTypeService.getRoomTypeDetail(1L);
        assertEquals("标准单人间", detail.getTypeName());
        assertFalse(detail.getImages().isEmpty());
        assertFalse(detail.getReviews().isEmpty());
        assertEquals("太极", detail.getReviews().get(0).getUsername());

        RoomTypeQueryDTO rtQuery = new RoomTypeQueryDTO();
        rtQuery.setPageNum(1);
        rtQuery.setPageSize(10);
        rtQuery.setMinPrice(new BigDecimal("200"));
        rtQuery.setMaxPrice(new BigDecimal("500"));
        PageResult rtPage = roomTypeService.pageQueryForUser(rtQuery);
        assertTrue(rtPage.getTotal() >= 3);

        // Existing room 301 is status=2 (occupied); duplicate batchInsert must not overwrite its status
        Room dupRoom = new Room();
        dupRoom.setRoomNumber("301");
        dupRoom.setRoomTypeId(1L);
        dupRoom.setFloor("9");
        dupRoom.setStatus(1);

        Room newRoom = new Room();
        newRoom.setRoomNumber("601");
        newRoom.setRoomTypeId(1L);
        newRoom.setFloor("6");
        newRoom.setStatus(1);

        roomMapper.batchInsert(List.of(dupRoom, newRoom));
        Room room301 = roomMapper.getByRoomNumber("301");
        assertEquals(2, room301.getStatus());
        assertEquals("3", room301.getFloor());

        Room room601 = roomMapper.getByRoomNumber("601");
        assertNotNull(room601);
        assertTrue(room601.getId() > 12L);

        Map<String, Object> currentCheckIn = roomMapper.getCurrentCheckInInfo(5L);
        assertNotNull(currentCheckIn);
        assertEquals("李四", currentCheckIn.get("guestName"));
        assertEquals("李四", roomService.getRoomDetailStatusById(5L).getGuestName());
        assertNotNull(roomService.getRoomDetailStatusById(5L).getCheckOutDate());
    }

    @Test
    void statsAndRecursiveCteQueriesWorkAcrossRanges() {
        LocalDate start = LocalDate.of(2025, 4, 1);
        LocalDate end = LocalDate.of(2025, 4, 30);

        assertFalse(statsMapper.getRoomSalesStats(start, end).isEmpty());
        assertTrue(statsMapper.getRoomCountByType(1L) >= 2);
        assertTrue(statsMapper.getActualOccupancyDays(1L, start, end) >= 1);
        assertFalse(statsMapper.getIncomeStatsByDay(start, end).isEmpty());
        assertFalse(statsMapper.getIncomeStatsByMonth(start, end).isEmpty());
        assertFalse(statsMapper.getIncomeStatsByYear(start, end).isEmpty());
        assertFalse(statsMapper.getExpenseStatsByMonth(start, end).isEmpty());
        assertFalse(statsMapper.getExpenseStatsByYear(start, end).isEmpty());
        assertEquals(1, statsMapper.getProfitStatsByMonth(start, end).size());
        assertEquals(1, statsMapper.getProfitStatsByYear(start, end).size());
        assertTrue(statsMapper.getTotalRoomCount() >= 12);

        // Inverted range returns empty list
        assertTrue(statsMapper.getOccupancyStatsByDay(end, start).isEmpty());

        // > 1000 days range works with SET_VAR(cte_max_recursion_depth=100000)
        LocalDate longStart = LocalDate.of(2023, 1, 1);
        LocalDate longEnd = LocalDate.of(2025, 12, 31);
        assertEquals(1096, statsMapper.getOccupancyStatsByDay(longStart, longEnd).size());
    }

    @Test
    void bookingCheckInAndCheckOutTransactionWorks() {
        LocalDate today = LocalDate.now();
        BookingOrder order = new BookingOrder();
        order.setOrderNo("ORTEST2026102401");
        order.setUserId(1L);
        order.setRoomTypeId(1L);
        order.setRoomPrice(new BigDecimal("258.00"));
        order.setCheckInDate(today);
        order.setCheckOutDate(today.plusDays(1));
        order.setDays(1);
        order.setGuestName("张三丰");
        order.setGuestPhone("13800138001");
        order.setGuestIdCard("110101199001011234");
        order.setTotalAmount(new BigDecimal("258.00"));
        order.setStatus(2); // PENDING_CHECK_IN
        orderMapper.insert(order);
        assertNotNull(order.getId());
        assertTrue(order.getId() > 4L);

        CheckInDTO checkInDTO = new CheckInDTO();
        checkInDTO.setOrderId(order.getId());
        checkInDTO.setRoomId(1L); // Room 201 (type 1, free)
        checkInDTO.setGuestName("张三丰");
        checkInDTO.setGuestPhone("13800138001");
        checkInDTO.setGuestIdCard("110101199001011234");
        checkInDTO.setDeposit(new BigDecimal("200.00"));
        checkInDTO.setRemark("集成测试入住");
        CheckInResultVO checkInResult = checkInService.checkIn(2L, checkInDTO);
        assertNotNull(checkInResult.getCheckInId());
        assertEquals(2, roomMapper.getById(1L).getStatus());

        CheckOutDTO checkOutDTO = new CheckOutDTO();
        checkOutDTO.setCheckInId(checkInResult.getCheckInId());
        checkOutDTO.setExtraFee(new BigDecimal("30.00"));
        checkOutDTO.setDamageFee(BigDecimal.ZERO);
        checkOutDTO.setPaymentMethod("支付宝");
        checkOutDTO.setRemark("集成测试退房");
        checkOutService.handleCheckOut(2L, checkOutDTO);

        assertEquals(3, roomMapper.getById(1L).getStatus());
        assertEquals(4, orderMapper.getById(order.getId()).getStatus());

        // Verify other mappers
        assertFalse(guestMapper.getGuestStayRecords("110101199001011234").isEmpty());
        assertFalse(favoriteMapper.pageQuery(1L).isEmpty());
        assertNotNull(attendanceMapper.getByEmployeeIdAndDate(2L, LocalDate.of(2025, 4, 10)));
        assertNotNull(performanceMapper.getByEmployeeIdAndMonth(2L, "2025-04"));

        OrderQueryDTO oq = new OrderQueryDTO();
        oq.setUserId(1L);
        oq.setPageNum(1);
        oq.setPageSize(10);
        assertFalse(orderMapper.pageQuery(oq).isEmpty());
    }
}
