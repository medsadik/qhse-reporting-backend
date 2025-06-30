package org.example.qhsereportingbackend.GlobalRepository;

import org.example.qhsereportingbackend.GlobalDto.NameObjectDto;
import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.example.qhsereportingbackend.IndicateursSST.Dto.BonnesPratiquesStatsDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.example.qhsereportingbackend.utils.DynamicQuery.*;
import static org.example.qhsereportingbackend.utils.TablesConstant.getKey;
import static org.example.qhsereportingbackend.utils.TablesConstant.tables;

@Repository
public class GlobalRepository {
    private final JdbcTemplate taskModifiedJdbcTemplate;
    private final JdbcTemplate formsJdbcTemplate;

    public GlobalRepository(@Qualifier("taskModifiedJdbcTemplate") JdbcTemplate taskModifiedJdbcTemplate,@Qualifier("formsJdbcTemplate") JdbcTemplate formsJdbcTemplate) {
        this.taskModifiedJdbcTemplate = taskModifiedJdbcTemplate;
        this.formsJdbcTemplate = formsJdbcTemplate;
    }

    public List<NameValueDto> getStatutBySD(String tableName,String projectPattern) {
        String sql = """
                SELECT
                    "SD" as name,
                    COUNT("SD") AS value,
                    ROUND(100.0 * COUNT("SD") / SUM(COUNT("SD")) OVER (), 2) AS percentage
                from (SELECT
                distinct on (id)
                    "SD"
                FROM
                    public."%s"
                WHERE
                    projet LIKE ?
                )
                group by "SD"
                order by value desc
                
                """.formatted(tableName);
        return getNameValueDataByDynamicQuery(
                taskModifiedJdbcTemplate,
                sql,
                new Object[]{projectPattern},
                "name",
                "value"
        );
    }

    public List<NameValueDto> getGlobalStatut(String tableName, String projectPattern) {
        String sql = """
            WITH sub AS (
                SELECT DISTINCT ON (id) id, "S0"
                FROM public."%s"
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

    public List<NameStatusDto> getGlobalStatuByColumn(String tableName, String projectPattern, String columnName,JdbcTemplate jdbcTemplate) {
        String columnFormatted = "\""+ columnName +"\"";
        String sql = """
                WITH sub AS (
                  SELECT DISTINCT ON (id)
                    id,"S0", %s, "Date de modification"
                  FROM public."%s"
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
                jdbcTemplate,
                sql,
                new Object[]{projectPattern},
                columnName,
                "count"
        );
    }

    public List<NameStatusDto> getHebdoStatuByColumn(String tableName, String projectPattern, String columnName, LocalDateTime startDate, LocalDateTime endDate, JdbcTemplate jdbcTemplate) {
        String columnFormatted = "\""+ columnName +"\"";
        String sql = """
                WITH sub AS (
                  SELECT DISTINCT ON (id)
                    id,"S0", %s, "Date de modification"
                  FROM public."%s"
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
                jdbcTemplate,
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
                    FROM public."%s"
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

    public List<NameValueDto> getTableStats(String tableName, String projectPattern, LocalDateTime startDate, LocalDateTime endDate, JdbcTemplate customJdbcTemplate){

        String table = getKey(tables, tableName);
        String sql = """
            WITH base_data AS (
                SELECT DISTINCT ON (id) *
                FROM public."%s"
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

            SELECT 'Total %s' AS name, COUNT(*) AS value FROM base_data
            UNION ALL
            SELECT 'IH - %s', COUNT(*) FROM date_filtered
            UNION ALL
            SELECT 'IH - CLOTURE %s', COUNT(*) FROM s0_approved
            UNION ALL
            SELECT 'S0 - SIGNALE %s', COUNT(*) FROM s0_signaled;
            """.formatted(tableName,table,table,table,table);
        return  getNameValueDataByDynamicQuery(customJdbcTemplate,
                sql,
                new Object[]{projectPattern,startDate,endDate},
                "name"
                ,"value");
    }


    public BonnesPratiquesStatsDto getBonnesPratiquesStats(String projetPattern, String type) {
        String sql = """
        WITH filtered AS (
            SELECT *
            FROM public."Fiche des Bonnes pratiques"
            WHERE projet LIKE ? AND "Type" = '%s'
        )
        SELECT
            COUNT(*) AS total_count,
            MAX("Date de création") AS latest_creation_date,
            ARRAY_AGG("Bonnes pratiques") AS bonnes_pratiques_list
        FROM filtered
        """.formatted(type);

        return formsJdbcTemplate.queryForObject(sql,new Object[]{projetPattern},(rs, rowNum) -> {
            int totalCount = rs.getInt("total_count");
            LocalDate latestCreationDate = rs.getDate("latest_creation_date") != null
                    ? rs.getDate("latest_creation_date").toLocalDate()
                    : null;

            Array sqlArray = rs.getArray("bonnes_pratiques_list");
            List<String> bonnesPratiques = sqlArray != null
                    ? Arrays.asList((String[]) sqlArray.getArray())
                    : Collections.emptyList();

            return new BonnesPratiquesStatsDto(totalCount, latestCreationDate, bonnesPratiques);
        });}

    public List<NameObjectDto> getFormationOrToolBoxStats(String tableName, String projetPattern, String categorie, LocalDateTime startDate, LocalDateTime endDate) {
        String duree;
        if(tableName.contains("Formations")) {
            duree = "Durée en H";
        }
        else {
            duree = "Durée en min";
        }
        String sql = """
                WITH base AS (
                  SELECT\s
                    id,
                    "Date",
                    "Date de création",
                    "Catégories",
                    "Thématiques",
                    "Nb d'effectif"::int AS nb_effectif,
                    "%s"::int AS duree
                  FROM public."%s"
                  WHERE projet LIKE ? AND "Catégories" = ?
                ),
                distinct_ids AS (
                  SELECT DISTINCT ON (id) *
                  FROM base
                ),
                date_filtered AS (
                  SELECT * FROM distinct_ids
                  WHERE "Date de création"::timestamp >= ? AND "Date de création"::timestamp < ?
                )
                SELECT 'Total %s' AS name, COUNT(*)::text AS value FROM distinct_ids
                UNION ALL
                SELECT 'Total Effectif', COALESCE(SUM(nb_effectif), 0)::text FROM distinct_ids
                UNION ALL
                SELECT 'Durée en H', COALESCE(SUM(duree), 0)::text FROM distinct_ids
                UNION ALL
                SELECT 'Dernière Date', TO_CHAR(MAX("Date"::date), 'YYYY-MM-DD') FROM distinct_ids
                UNION ALL
                SELECT 'IH - %s', COUNT(*)::text FROM date_filtered
                UNION ALL
                SELECT 'IH - Effectif %s', COALESCE(SUM(nb_effectif), 0)::text FROM date_filtered
                UNION ALL
                SELECT 'IH - %s', COALESCE(SUM(duree), 0)::text FROM date_filtered;
                """.formatted(duree,tables.get(tableName),tableName,tableName,tableName,duree);
        return getNameObjetDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern,categorie,startDate,endDate},
                "name",
                "value"
        );
    }

    public List<NameValueDto> getTotalThematiques(String tableName, String projetPattern,String categorie) {
        String sql = """
                SELECT
                  "Thématiques" as name,
                  COUNT(*) AS value
                FROM
                  public."%s"
                  WHERE projet LIKE ?
                    AND "Catégories" = ?
                GROUP BY
                  "Thématiques"
                ORDER BY
                  value DESC;
                """.formatted(tables.get(tableName));
        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern,categorie},
                "name",
                "value"
        );
    }

    public List<NameValueDto> getHebdoThematiques(String tableName, String projetPattern,LocalDateTime startDate, LocalDateTime endDate,String categorie) {
        String sql = """
                SELECT
                  "Thématiques" as name,
                  COUNT(*) AS value
                FROM
                  public."%s"
                  WHERE projet LIKE ?
                    AND "Catégories" = ? AND "Date de création"::timestamp >= ? AND "Date de création"::timestamp <= ?
                GROUP BY
                  "Thématiques"
                ORDER BY
                  value DESC;
                """.formatted(tables.get(tableName));
        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern,categorie,startDate,endDate},
                "name",
                "value"
        );
    }
}
