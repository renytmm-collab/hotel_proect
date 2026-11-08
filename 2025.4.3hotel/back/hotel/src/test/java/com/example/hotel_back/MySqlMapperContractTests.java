package com.example.hotel_back;

import com.example.hotel_back.Pojo.Entity.Room;
import com.example.hotel_back.mapper.RoomMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Parses production mappers without Spring, credentials, database access or startup side effects. */
class MySqlMapperContractTests {
    private static Configuration configuration;

    @BeforeAll
    static void parseMappers() throws Exception {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.getTypeAliasRegistry().registerAliases("com.example.hotel_back.Pojo.Entity");
        try (var paths = Files.list(Path.of("src/main/resources/mapper"))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".xml")).sorted().toList()) {
                try (var input = Files.newInputStream(path)) {
                    new XMLMapperBuilder(input, configuration, path.toString(), configuration.getSqlFragments()).parse();
                }
            }
        }
    }

    private String sql(String mapper, String statement, Map<String, Object> parameters) {
        return configuration.getMappedStatement("com.example.hotel_back.mapper." + mapper + "." + statement)
                .getBoundSql(parameters).getSql().replaceAll("\\s+", " ").trim();
    }

    @Test
    void allSingleRowInsertsReturnGeneratedKeys() {
        for (Object item : configuration.getMappedStatements()) {
            if (!(item instanceof MappedStatement statement) || statement.getSqlCommandType() != SqlCommandType.INSERT
                    || statement.getId().endsWith(".batchInsert")) continue;
            assertInstanceOf(Jdbc3KeyGenerator.class, statement.getKeyGenerator(), statement.getId());
            assertArrayEquals(new String[]{"id"}, statement.getKeyProperties(), statement.getId());
        }
    }

    @Test
    void endDayFilterIsExclusiveNextMidnight() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", LocalDate.of(2024, 2, 28));
        parameters.put("endDate", LocalDate.of(2024, 2, 29));
        String query = sql("StatsMapper", "getIncomeStatsByDay", parameters);
        assertTrue(query.contains("payment_time >= DATE(?)"));
        assertTrue(query.contains("payment_time < DATE_ADD(DATE(?), INTERVAL 1 DAY)"));
    }

    @Test
    void dateSeriesAndOccupancyKeepTheirBoundaryRules() {
        Map<String, Object> parameters = Map.of("startDate", LocalDate.of(2023, 12, 31),
                "endDate", LocalDate.of(2024, 1, 2));
        for (String statement : List.of("getProfitStatsByMonth", "getProfitStatsByYear", "getOccupancyStatsByDay")) {
            String query = sql("StatsMapper", statement, parameters);
            assertTrue(query.contains("WITH RECURSIVE"));
            assertTrue(query.contains("WHERE DATE(?) <= DATE(?)"));
            assertFalse(query.contains("CONNECT BY"));
        }
        String occupied = sql("StatsMapper", "getOccupancyStatsByDay", parameters);
        assertTrue(occupied.contains("check_out_time >= DATE_ADD(dd.full_date, INTERVAL 1 DAY)"));
    }

    @Test
    void roomBatchSkipsDuplicatesWithoutOverwritingBusinessFields() {
        Room room = new Room();
        room.setRoomNumber("101");
        room.setRoomTypeId(1L);
        room.setFloor("1");
        room.setStatus(1);
        String query = sql("RoomMapper", "batchInsert", Map.of("list", List.of(room, room)));
        assertTrue(query.endsWith("ON DUPLICATE KEY UPDATE id = room.id"));
        assertEquals(8, configuration.getMappedStatement(RoomMapper.class.getName() + ".batchInsert")
                .getBoundSql(Map.of("list", List.of(room, room))).getParameterMappings().size());
    }

    @Test
    void hotRoomsAndLatestCheckinUseDeterministicOrdering() {
        String hot = sql("RoomTypeMapper", "getHotRoomTypes", Map.of("limit", 3));
        assertTrue(hot.contains("ROW_NUMBER() OVER"));
        assertTrue(hot.contains("ORDER BY t.weight DESC, t.id ASC LIMIT ?"));
        String checkin = sql("RoomMapper", "getCurrentCheckInInfo", Map.of("roomId", 1L));
        assertTrue(checkin.endsWith("ORDER BY ci.check_in_time DESC, ci.id DESC LIMIT 1"));
    }

    @Test
    void userNameFilterBuildsValidWhereClause() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("realName", "test");
        String query = sql("UserMapper", "userPageQuery", parameters);
        assertTrue(query.contains("WHERE real_name like CONCAT('%', ?, '%')"));
    }
}
