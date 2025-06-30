package org.example.qhsereportingbackend.IndicateursSST.Dao;

import org.example.qhsereportingbackend.GlobalDto.NameObjectDto;
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
import java.util.Map;

import static org.example.qhsereportingbackend.utils.DynamicQuery.getNameObjetDataByDynamicQuery;
import static org.example.qhsereportingbackend.utils.DynamicQuery.getNameValueDataByDynamicQuery;

@Repository
public class IndicateurSstDao {

    private final JdbcTemplate formsJdbcTemplate;
    private final Map<String,String> tables = Map.of("Formations","Fiche Formation","Toolbox","Fiche Toolbox");
    public IndicateurSstDao(@Qualifier("formsJdbcTemplate") JdbcTemplate taskModifiedJdbcTemplate) {
        this.formsJdbcTemplate = taskModifiedJdbcTemplate;
    }

    public BonnesPratiquesStatsDto getBonnesPratiquesStats(String projetPattern,String type) {
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

    public List<NameValueDto> getReunionInfo(String projetPattern, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT
                  CASE
                    WHEN "Date de création"::timestamp >= ? AND "Date de création"::timestamp <=  ? THEN 'IH'
                    ELSE 'Total'
                  END AS name,
                  COUNT(DISTINCT id) AS value
                FROM public."Compte rendu réunion HSE"
                WHERE projet LIKE ?
                GROUP BY name
                """;
        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{startDate,endDate,projetPattern},
                "name",
                "value"

        );
    }

    public List<NameValueDto> getReclamationInfo(String projetPattern) {
        String sql = """
                SELECT
                  COUNT(DISTINCT id) AS value,
                  "Réclamation concerne" as name
                FROM
                  public."Fiche de réclamation"
                WHERE
                  projet LIKE ?
                GROUP BY
                  "Réclamation concerne";
                """;
        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern},
                "name",
                "value"
        );
    }

    public List<NameObjectDto> getFormationOrToolBoxStats(String tableName, String projetPattern,String categorie, LocalDateTime startDate, LocalDateTime endDate) {
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

    public List<NameObjectDto> getRecompenses(String projetPattern) {
        String sql = """
                SELECT
                  'Max Date de création' AS name,
                  MAX("Date de création"::date)::text AS value
                FROM
                  public."Fiche Casque vert"
                WHERE
                  projet LIKE ?
                
                UNION ALL
                
                SELECT
                  'Total Récompenses',
                  COUNT(*)::text
                FROM
                  public."Fiche Casque vert"
                WHERE
                  projet LIKE ?
                
                UNION ALL
                
                SELECT
                  'Décision: ' || "Décision",
                  COUNT(*)::text
                FROM
                  public."Fiche Casque vert"
                WHERE
                  projet LIKE ?
                GROUP BY
                  "Décision";
                """;
        return getNameObjetDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern,projetPattern,projetPattern},
                "name",
                "value"
        );
    }

    public List<NameObjectDto> getSanctions(String projetPattern) {
        String sql = """
                SELECT
                  'Max Date de création' AS name,
                  MAX("Date de création"::date)::text AS value
                FROM
                  public."Fiche de sanction"
                WHERE
                  projet LIKE ?
                
                UNION ALL
                
                SELECT
                  'Total Sanctions' AS name,
                  COUNT(*)::text AS value
                FROM
                  public."Fiche de sanction"
                WHERE
                  projet LIKE ?
                
                UNION ALL
                
                SELECT
                  'Décision: ' || "Décision" AS name,
                  COUNT(*)::text AS value
                FROM
                  public."Fiche de sanction"
                WHERE
                  projet LIKE ?
                GROUP BY
                  "Décision"
                ORDER BY
                  name; 
                
                """;
        return getNameObjetDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern,projetPattern,projetPattern},
                "name",
                "value"
        );

    }

    public List<NameObjectDto> getSuInfo(String projetPattern) {

        String sql = """
                SELECT 'Nb Su' AS name, COUNT(DISTINCT id)::TEXT AS value
                FROM public."Fiche Simulation d'urgence"
                WHERE projet LIKE ?
                
                UNION ALL
                
                SELECT 'IM Su' AS name, COUNT(DISTINCT id)::TEXT AS value
                FROM public."Fiche Simulation d'urgence"
                WHERE projet LIKE ?
                  AND "Date de création"::timestamp >= date_trunc('month', CURRENT_DATE)
                  AND "Date de création"::timestamp < (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month')
                
                UNION ALL
                
                SELECT 'Date Simulation' AS name, "Date Simulation d'urgence"::TEXT AS value
                FROM (
                    SELECT "Date Simulation d'urgence"
                    FROM public."Fiche Simulation d'urgence"
                    WHERE projet LIKE ?
                    ORDER BY "Date Simulation d'urgence" DESC
                    LIMIT 1
                ) AS sub1
                
                UNION ALL
                
                SELECT 'Thème Simulation' AS name, "Thème de la simulation"::TEXT AS value
                FROM (
                    SELECT "Thème de la simulation"
                    FROM public."Fiche Simulation d'urgence"
                    WHERE projet LIKE ?
                    ORDER BY "Date Simulation d'urgence" DESC
                    LIMIT 1
                ) AS sub2;
                """;

        return getNameObjetDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern,projetPattern,projetPattern,projetPattern},
                "name",
                "value"
        );
    }

    public List<NameValueDto> getGlobalTableInfoGrouped(String tableName,String groupedColumn,String count , String projetPattern) {
        String sql = """
                SELECT "%s" as name, COUNT(%s) as value
                FROM public."%s"
                WHERE "projet" LIKE ?
                GROUP BY "%s";
                """.formatted(groupedColumn,count,tableName,groupedColumn);

        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern},
                "name",
                "value"
        );
    }

    public List<NameValueDto> getMensuelTableInfoGrouped(String groupedColumn,String count ,String projetPattern,LocalDate start,LocalDate end) {
        String sql = """
                SELECT "%s" as name, COUNT(%s) as value
                    FROM public."Fiche Simulation d'urgence"
                    WHERE "projet" LIKE ?
                      AND to_timestamp("Date de création", 'YYYY-MM-DD HH24:MI:SS') >= ? - INTERVAL '30 days'
                      AND to_timestamp("Date de création", 'YYYY-MM-DD HH24:MI:SS') <= ?
                    GROUP BY "%s";
                """.formatted(groupedColumn,count,groupedColumn);

        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern,end,end},
                "name",
                "value"
        );
    }

    public List<NameValueDto> getHebdoTableInfoGrouped(String tableName,String groupedColumn,String count, String projetPattern, LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT "%s" as name, COUNT(%s) as value
                    FROM public."%s"
                    WHERE "projet" LIKE ?
                and "Date de création"::timestamp >= ?
                and "Date de création"::timestamp  <=  ?
                    GROUP BY "%s";
                """.formatted(groupedColumn,count,tableName,groupedColumn);

        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern,start,end},
                "name",
                "value"
        );
    }

    public NameValueDto getTotalRecrutementLocal(String projetPattern) {
        String sql = """
        SELECT COUNT(*) AS total 
        FROM public."Fiche suivi du recrutement local"
        WHERE projet LIKE ? AND "Date de sortie" IS NULL
    """;

        return formsJdbcTemplate.queryForObject(
                sql,
                new Object[]{projetPattern},
                (rs, rowNum) -> new NameValueDto("Recrutement local Total", rs.getInt("total"))
        );
    }

    public NameValueDto getTotalRecrutementLocalADate(String projetPattern,LocalDate start,LocalDate end) {
        String sql = """
                SELECT
    ROUND(
        COUNT(*)::decimal / (
            SELECT "T. Eff./J"::decimal
            FROM public."Fiche d'effectifs"
            WHERE projet LIKE ?
                AND "Date"::date >= ?
              AND "Date"::date <= ?
            ORDER BY "Date" DESC
            LIMIT 1
        ) * 100, 2
    ) AS ratio_percentage
FROM public."Fiche suivi du recrutement local"
WHERE projet LIKE ?
  AND "Date de sortie" IS NULL
    """;

        return formsJdbcTemplate.queryForObject(
                sql,
                new Object[]{projetPattern,start,end,projetPattern},
                (rs, rowNum) -> new NameValueDto("Recrutement Local A Date", rs.getInt("ratio_percentage"))
        );
    }

    public List<NameValueDto> getParcStats(String projetPattern) {
        String sql = """
                SELECT 'Total Parc' AS name, COUNT(*) AS value
                FROM public."Fiche suivi du parc matériel"
                WHERE projet LIKE ?
                UNION ALL
                SELECT 'Parc actif' AS name, COUNT(*) AS value
                FROM public."Fiche suivi du parc matériel"
                WHERE projet LIKE ? AND "Date de sortie" IS NULL;
                """;

        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern,projetPattern},
                "name",
                "value"
        );
    }
    public List<NameValueDto> getParcTypologies(String projetPattern) {
        String sql = """
                    SELECT "Catégorie de l'engin" as name,count(*) as value
                    FROM public."Fiche suivi du parc matériel"
                    where projet like ? and "Date de sortie" is null
                    group by "Catégorie de l'engin"
                    order by value desc
                """;

        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projetPattern},
                "name",
                "value"
        );
    }

}
