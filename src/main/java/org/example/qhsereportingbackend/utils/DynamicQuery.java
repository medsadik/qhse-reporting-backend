package org.example.qhsereportingbackend.utils;

import org.example.qhsereportingbackend.GlobalDto.NameObjectDto;
import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class DynamicQuery {

    public static List<NameValueDto> getNameValueDataByDynamicQuery(
            JdbcTemplate jdbcTemplate,
            String sql,
            Object[] params,
            String nameColumn,
            String valueColumn
    ) {
        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new NameValueDto(rs.getString(nameColumn), rs.getDouble(valueColumn))
        );
    }    public static List<NameObjectDto> getNameObjetDataByDynamicQuery(
            JdbcTemplate jdbcTemplate,
            String sql,
            Object[] params,
            String nameColumn,
            String valueColumn
    ) {
        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new NameObjectDto(rs.getString(nameColumn), rs.getString(valueColumn))
        );
    }

    public static List<NameStatusDto> getNameStatusDtos(JdbcTemplate jdbcTemplate,String sql, Object[] params, String columnName, String columnValue) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql,
                params

        );

        Map<String, Map<String, Integer>> grouped = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            String name = (String) row.get(columnName);
            String statusGroup = "status_group";
            String status = (String) row.get(statusGroup);
            Integer count = ((Number) row.get(columnValue)).intValue();

            grouped.computeIfAbsent(name, k -> new LinkedHashMap<>())
                    .put(status, count);
        }

        return grouped.entrySet().stream()
                .map(e -> new NameStatusDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public static void addAverageScore(List<NameValueDto> nameValueMap, String name) {
        double avgRounded = calculateAverageScore(nameValueMap);
        NameValueDto total = new NameValueDto(name,avgRounded);
        nameValueMap.add(total);
    }

    public static double calculateAverageScore(List<NameValueDto> nameValueMap) {
        OptionalDouble average = nameValueMap.stream().mapToDouble(NameValueDto::value).average();
        return BigDecimal.valueOf(average.orElse(0)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

}
