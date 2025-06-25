package org.example.qhsereportingbackend.IndicateursQualite.Dao;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.example.qhsereportingbackend.utils.DynamicQuery.getNameStatusDtos;
import static org.example.qhsereportingbackend.utils.DynamicQuery.getNameValueDataByDynamicQuery;

@Repository
public class IndicateurQualiteDao {
    private final JdbcTemplate taskModifiedJdbcTemplate;

    public IndicateurQualiteDao(@Qualifier("taskModifiedJdbcTemplate") JdbcTemplate taskModifiedJdbcTemplate) {
        this.taskModifiedJdbcTemplate = taskModifiedJdbcTemplate;
    }

    public Map<String,Integer> getTotalQuality(String projectPattern) {
        String sql = """
            SELECT "Conformité", COUNT(*) AS count
            FROM (
                SELECT DISTINCT ON (id) id, "Conformité"
                FROM public."Qualité - Assurance & Contrôle V5"
                WHERE projet LIKE ?
                ORDER BY id, "Date de modification" DESC
            ) AS latest_records
            GROUP BY "Conformité"
            
            UNION ALL
            
            SELECT 'Total', COUNT(*)
            FROM (
                SELECT DISTINCT ON (id) id, "Conformité"
                FROM public."Qualité - Assurance & Contrôle V5"
                WHERE projet LIKE ?
                ORDER BY id, "Date de modification" DESC
            ) AS latest_records;
""";

        Object[] params = new Object[]{projectPattern, projectPattern};

        return taskModifiedJdbcTemplate.query(sql, params,rs -> {
            Map<String, Integer> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("Conformité"), rs.getInt("count"));
            }
            return result;
        });
    }

    public Map<String, Integer> getTotalQualityHebdo(String projectPattern, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT "Conformité", COUNT(*) AS count
            FROM (
                SELECT DISTINCT ON (id) id, "Conformité"
                FROM public."Qualité - Assurance & Contrôle V5"
                WHERE projet LIKE ? and "Date de création"::timestamp >= ? and "Date de création"::timestamp <= ?
                ORDER BY id, "Date de modification" DESC
            ) AS latest_records
            GROUP BY "Conformité"
            
            UNION ALL
            
            SELECT 'Total', COUNT(*)
            FROM (
                SELECT DISTINCT ON (id) id, "Conformité"
                FROM public."Qualité - Assurance & Contrôle V5"
                WHERE projet LIKE ? and "Date de création"::timestamp >= ? and "Date de création"::timestamp <= ?
                ORDER BY id, "Date de modification" DESC
            ) AS latest_records;
""";

        Object[] params =  new Object[]{projectPattern, startDate, endDate,projectPattern, startDate, endDate};

        return taskModifiedJdbcTemplate.query(sql, params,rs -> {
            Map<String, Integer> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("Conformité"), rs.getInt("count"));
            }
            return result;
        });
    }

    public Map<String, Integer> getTotalQualityHebdoClosed(String projectPattern, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """

            SELECT "Conformité", COUNT(*) AS count
            FROM (
                SELECT DISTINCT ON (id) id, "Conformité"
                FROM public."Qualité - Assurance & Contrôle V5"
                WHERE projet LIKE ? and "Date de modification"::timestamp >= ? and "Date de modification"::timestamp <= ? and "S0" like 'Approuvé%'
                ORDER BY id, "Date de modification" DESC
            
            
            ) AS latest_records
            GROUP BY "Conformité"
            
            UNION ALL
            
            SELECT 'Total', COUNT(*)
            FROM (
                SELECT DISTINCT ON (id) id, "Conformité"
                FROM public."Qualité - Assurance & Contrôle V5"
                WHERE projet LIKE ? and "Date de modification"::timestamp >= ? and "Date de modification"::timestamp <= ? and "S0" like 'Approuvé%'
            ) AS latest_records;
""";

        Object[] params =  new Object[]{projectPattern, startDate, endDate,projectPattern, startDate, endDate};

        return taskModifiedJdbcTemplate.query(sql, params,rs -> {
            Map<String, Integer> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("Conformité"), rs.getInt("count"));
            }
            return result;
        });
    }

    public List<NameValueDto> getConformiteStats(String projectPattern) {
        if (projectPattern == null || projectPattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Project pattern cannot be null or empty");
        }

        String sql = """
        
                WITH sub AS (
             SELECT DISTINCT ON (id) id, "Conformité"
             FROM public."Qualité - Assurance & Contrôle V5"
             WHERE projet LIKE ?
             ORDER BY id, "Date de modification" DESC
         ),
         total AS (
             SELECT COUNT(*) AS total_count FROM sub
         )
         SELECT 
             s."Conformité" AS name, 
             COUNT(*) AS value
         FROM sub s, total t
         GROUP BY s."Conformité", t.total_count
         ORDER BY COUNT(*) DESC
        """;
        return getNameValueDataByDynamicQuery(taskModifiedJdbcTemplate,sql,new Object[]{projectPattern},"name","value");
    }

    public List<NameValueDto> getACByControlType(String projectPattern) {
        String sql = """
                SELECT "Type Contrôle", COUNT(DISTINCT id) AS total
                 FROM public."Qualité - Assurance & Contrôle V5"
                 WHERE projet LIKE ? AND "Type Contrôle" IN ('Extérieur', 'Externe', 'Interne')
                 GROUP BY "Type Contrôle"
                """;
        return getNameValueDataByDynamicQuery(taskModifiedJdbcTemplate,sql,new Object[]{projectPattern},"Type Contrôle","total");
    }

    public List<NameValueDto> getHebdoACByControlType(String projectPattern, LocalDateTime startDate, LocalDateTime endDate) {
        String sql =
                        """
        SELECT "Type Contrôle", COUNT(DISTINCT id) AS total
         FROM public."Qualité - Assurance & Contrôle V5"
         WHERE projet LIKE ?
           AND "Type Contrôle" IN ('Extérieur', 'Externe', 'Interne')
           AND "Date de création"::timestamp >= ? and "Date de création"::timestamp <= ?
         GROUP BY "Type Contrôle"
""";
        return getNameValueDataByDynamicQuery(taskModifiedJdbcTemplate,
                sql,
                new Object[]{projectPattern,startDate,endDate},
                "Type Contrôle"
                ,"total");
    }

    public List<NameValueDto>  getClotureStatut(String projectPattern,String ConformityStatus) {
        String sql = """
                WITH sub AS (
                     SELECT DISTINCT ON (id) id, "Conformité", "S0"
                     FROM public."Qualité - Assurance & Contrôle V5"
                     WHERE projet LIKE ?
                     ORDER BY id, "Date de modification" DESC
                 )
                 SELECT
                     CASE
                         WHEN "S0" ILIKE '%Approuvé%' THEN 'Approuvé'
                         ELSE "S0"
                     END AS status_group,
                     COUNT(*) 
                 FROM sub
                 WHERE "Conformité" = ?
                 GROUP BY status_group;
                """;
        return getNameValueDataByDynamicQuery(taskModifiedJdbcTemplate,
                sql,
                new Object[]{projectPattern,ConformityStatus},
                "status_group"
                ,"count");    }

    public List<NameStatusDto> getHebdoCloturedByFamille(String projectPattern, String conformityStatus, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
        WITH sub AS (
            SELECT DISTINCT ON (id)
                id, "Conformité", "S0", "Métiers", "Date de modification"
            FROM public."Qualité - Assurance & Contrôle V5"
            WHERE projet LIKE ?
              AND "Conformité" = ?
              AND "Date de modification"::timestamp >= ?
              AND "Date de modification"::timestamp <= ?
            ORDER BY id, "Date de modification"::timestamp DESC
        )
        SELECT 
            "Métiers",
            CASE 
                WHEN "S0" ILIKE '%Approuvé%' THEN 'Approuvé'
                ELSE "S0"
            END AS status_group,
            COUNT(*) AS count
        FROM sub
        GROUP BY "Métiers", status_group
        ORDER BY "Métiers", status_group
    """;
    return getNameStatusDtos(
            taskModifiedJdbcTemplate,
            sql,
            new Object[]{                projectPattern,
                conformityStatus,
                startDate,
                endDate},
                "Métiers",
                "count"
    );
    }

    public List<NameStatusDto> getCloturedByFamille(String projetPattern, String conformityStatus) {
        String sql = """

                WITH sub AS (
    SELECT DISTINCT ON (id) id, "Conformité", "S0", "Métiers"
    FROM public."Qualité - Assurance & Contrôle V5"
    WHERE projet LIKE ?
    ORDER BY id, "Date de modification" DESC
)
SELECT\s
    "Métiers",
    CASE\s
        WHEN "S0" ILIKE '%Approuvé%' THEN 'Approuvé'
        ELSE "S0"
    END AS status_group,
    COUNT(*) AS count
FROM sub
WHERE "Conformité" = ?
GROUP BY "Métiers", status_group
ORDER BY "Métiers", status_group;

    """;

        return getNameStatusDtos(taskModifiedJdbcTemplate, sql,new Object[]{projetPattern, conformityStatus}, "Métiers", "count");
    }


    public List<NameValueDto> getReactivityByFamille(String projectPattern, String conformityStatus) {
        String sql =
                """
                        WITH sub AS (
                            SELECT DISTINCT ON (id)
                                id,
                                "Conformité",
                                "Métiers",
                                "Date de modification",
                                "Date de création"
                            FROM public."Qualité - Assurance & Contrôle V5"
                            WHERE projet LIKE ?
                            ORDER BY id, "Date de modification" DESC
                        )
                        SELECT
                            "Métiers",
                            COUNT(*) AS count,
                            ROUND(AVG("Date de modification"::date - "Date de création"::date)::numeric, 3) AS reactivite
                        FROM sub
                        WHERE "Conformité" = ?
                        GROUP BY "Métiers"
                        ORDER BY "Métiers";
                        
""";
        return getNameValueDataByDynamicQuery(taskModifiedJdbcTemplate,
                sql,
                new Object[]{projectPattern,conformityStatus},
                "Métiers"
                ,"reactivite");
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
    public List<NameStatusDto> getHebdoStatuByColumn(String tableName, String projectPattern, String columnName,LocalDateTime startDate, LocalDateTime endDate) {
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


    public List<NameStatusDto> getHebdoStatusByTableAndMetiers(String table, String projetPattern, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                WITH sub AS (
                  SELECT DISTINCT ON (id)
                    id,"S0", "Métiers", "Date de modification"
                  FROM public.%s
                  WHERE projet LIKE ?
                    AND "Date de modification"::timestamp >= ?
                    AND "Date de modification"::timestamp <= ?
                  ORDER BY id, "Date de modification"::timestamp DESC
                )
                SELECT
                  "Métiers",
                  CASE
                    WHEN "S0" ILIKE '%%Approuvé%%' THEN 'Approuvé'
                    ELSE "S0"
                  END AS status_group,
                  COUNT(*) AS count
                FROM sub
                GROUP BY "Métiers", status_group
                ORDER BY "Métiers", status_group;
                """.formatted(table);

        return getNameStatusDtos(
                taskModifiedJdbcTemplate,
                sql,
                new Object[]{projetPattern,startDate,endDate},
                "Métiers",
                "count"
        );
    }


}


