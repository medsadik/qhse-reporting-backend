package org.example.qhsereportingbackend.GlobalRepository;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.qhsereportingbackend.utils.DynamicQuery.getNameStatusDtos;
import static org.example.qhsereportingbackend.utils.DynamicQuery.getNameValueDataByDynamicQuery;

@Repository
public class GlobalRepository {
    private final JdbcTemplate taskModifiedJdbcTemplate;

    public GlobalRepository(@Qualifier("taskModifiedJdbcTemplate") JdbcTemplate taskModifiedJdbcTemplate) {
        this.taskModifiedJdbcTemplate = taskModifiedJdbcTemplate;
    }
    public List<NameValueDto> getGlobalStatut(String tableName, String projectPattern) {
        String sql = """
            WITH sub AS (
                SELECT DISTINCT ON (id) id, "S0"
                FROM public.%s
                WHERE projet LIKE ?
                ORDER BY id, "Date de modification" DESC
            )
            SELECT
                CASE
                    WHEN "S0" ILIKE '%%Approuvé%%' THEN 'Approuvé'
                    ELSE "S0"
                END AS name,
                COUNT(*) AS value
            FROM sub
            GROUP BY name
            """.formatted(tableName);

        return getNameValueDataByDynamicQuery(
                taskModifiedJdbcTemplate,
                sql,
                new Object[]{projectPattern},
                "name",
                "value"
        );
    }

    public List<NameStatusDto> getGlobalStatuByColumn(String tableName, String projectPattern, String columnName) {
        String columnFormatted = "\""+ columnName +"\"";
        String sql = """
                WITH sub AS (
                  SELECT DISTINCT ON (id)
                    id,"S0", %s, "Date de modification"
                  FROM public.%s
                  WHERE projet LIKE ?
                  ORDER BY id, "Date de modification"::timestamp DESC
                )
                SELECT\s
                  %s,
                  CASE\s
                    WHEN "S0" ILIKE '%%Approuvé%%' THEN 'Approuvé'
                    ELSE "S0"
                  END AS status_group,
                  COUNT(*) AS count
                FROM sub
                GROUP BY %s, status_group
                ORDER BY %s, status_group,count;
                """.formatted(columnFormatted,tableName,columnFormatted,columnFormatted,columnFormatted);
        return getNameStatusDtos(
                taskModifiedJdbcTemplate,
                sql,
                new Object[]{projectPattern},
                columnName,
                "count"
        );
    }

    public List<NameStatusDto> getHebdoStatuByColumn(String tableName, String projectPattern, String columnName, LocalDateTime startDate, LocalDateTime endDate) {
        String columnFormatted = "\""+ columnName +"\"";
        String sql = """
                WITH sub AS (
                  SELECT DISTINCT ON (id)
                    id,"S0", %s, "Date de modification"
                  FROM public.%s
                  WHERE projet LIKE ?
                    AND "Date de modification"::timestamp >= ?
                    AND "Date de modification"::timestamp <= ?
                  ORDER BY id, "Date de modification"::timestamp DESC
                )
                SELECT\s
                  %s,
                  CASE\s
                    WHEN "S0" ILIKE '%%Approuvé%%' THEN 'Approuvé'
                    ELSE "S0"
                  END AS status_group,
                  COUNT(*) AS count
                FROM sub
                GROUP BY %s, status_group
                ORDER BY %s, status_group,count;
                """.formatted(columnFormatted,tableName,columnFormatted,columnFormatted,columnFormatted);
        return getNameStatusDtos(
                taskModifiedJdbcTemplate,
                sql,
                new Object[]{projectPattern,startDate,endDate},
                columnName,
                "count"
        );
    }

    public List<NameValueDto> getReactiviteByTable(String tableName,String projectPattern,String columnName) {
        String columnFormatted = "\""+ columnName +"\"";
        String sql = """
                WITH sub AS (
                    SELECT DISTINCT ON (id)
                        id,
                        %s,
                        "Date de modification",
                        "Date de création"
                    FROM public.%s
                    WHERE projet LIKE ?
                    ORDER BY id, "Date de modification" DESC
                )
                SELECT
                    %s, 
                    COUNT(*) AS count,
                    ROUND(AVG("Date de modification"::date - "Date de création"::date)::numeric, 3) AS reactivite
                FROM sub
                GROUP BY %s          
                ORDER BY %s;
                """.formatted(columnFormatted,tableName,columnFormatted,columnFormatted,columnFormatted);
        return getNameValueDataByDynamicQuery(
                taskModifiedJdbcTemplate,
                sql,
                new Object[]{projectPattern},
                columnName,
                "reactivite"
        );
    }

    public List<NameValueDto> getTableStats(String tableName, String projectPattern, LocalDateTime startDate, LocalDateTime endDate){
        String sql = """
            WITH base_data AS (
                SELECT DISTINCT ON (id) *
                FROM public.%s
                WHERE projet LIKE ?
                ORDER BY id, "Date de modification" DESC
            ),

            date_filtered AS (
                SELECT *
                FROM base_data
                WHERE "Date de création"::timestamp >= ?
                  AND "Date de création"::timestamp <= ?
            ),

            s0_approved AS (
                SELECT *
                FROM date_filtered
                WHERE "S0" LIKE 'App%%'
            ),

            s0_signaled AS (
                SELECT *
                FROM date_filtered
                WHERE "S0" LIKE 'Signal%%'
            )

            SELECT 'Total' AS name, COUNT(*) AS value FROM base_data
            UNION ALL
            SELECT 'IH -', COUNT(*) FROM date_filtered
            UNION ALL
            SELECT 'IH - CLOTURE', COUNT(*) FROM s0_approved
            UNION ALL
            SELECT 'S0 - SIGNALE', COUNT(*) FROM s0_signaled;
            """.formatted(tableName);
        return  getNameValueDataByDynamicQuery(taskModifiedJdbcTemplate,
                sql,
                new Object[]{projectPattern,startDate,endDate},
                "name"
                ,"value");
    }

}
