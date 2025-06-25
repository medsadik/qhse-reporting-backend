package org.example.qhsereportingbackend.IndicateursGeneraux.Dao;

import org.example.qhsereportingbackend.IndicateursGeneraux.Dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static java.lang.Math.round;


@Repository
public class IndicateursGenerauxDao {

    private final JdbcTemplate formsJdbcTemplate;
    private final JdbcTemplate taskModifiedJdbcTemplate;

    public IndicateursGenerauxDao(@Qualifier("formsJdbcTemplate") JdbcTemplate formsJdbcTemplate,
                                      @Qualifier("taskModifiedJdbcTemplate")JdbcTemplate taskModifiedJdbcTemplate) {
        this.formsJdbcTemplate = formsJdbcTemplate;
        this.taskModifiedJdbcTemplate = taskModifiedJdbcTemplate;
    }


    public List<EffectifByDate> getEffectifByDate(String projet) {
        String sqlForm = """
        SELECT "Date", "T. Eff./J"
        FROM public."Fiche d'effectifs"
        WHERE projet LIKE ? and "Date" is not null
        ORDER BY "Date"
    """;

        String sqlModified = """
        SELECT "Date", "T. Eff./J"
        FROM public."Effectifs V5"
        WHERE projet LIKE ? and "Date" is not null
        ORDER BY "Date"
    """;

        Map<String, String> mergedMap = new LinkedHashMap<>();

        // First table
        List<Map<String, Object>> formsResults = formsJdbcTemplate.queryForList(sqlForm, projet + "%");
        List<Map<String, Object>> modifiedResults = taskModifiedJdbcTemplate.queryForList(sqlModified, projet + "%");
        for (Map<String, Object> row : formsResults) {
            String dateStr = row.get("Date").toString();
            mergedMap.put(dateStr, row.get("T. Eff./J") == null ? "0" :row.get("T. Eff./J").toString());
        }

        // Second table (overwrites duplicates if dates match)
        for (Map<String, Object> row : modifiedResults) {
            String dateStr = row.get("Date").toString();
            mergedMap.put(dateStr, row.get("T. Eff./J") == null ? "0" :row.get("T. Eff./J").toString());
        }

        // Convert to DTO list
        List<EffectifByDate> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : mergedMap.entrySet()) {
            result.add(new EffectifByDate(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    public List<AvancementStat> getAvancementStats(String projet, LocalDateTime endDateTime) {

        String sql = """
        SELECT
            "Date",
            "Date de création"::date,
            "Taux Avancement prévisionnel",
            "Taux Avancement réel"
        FROM
            public."Fiche suivi d'avancement des travaux"
        WHERE
            projet LIKE ?
            AND "Date de création"::timestamp < ?
            AND "Date" IS NOT NULL
        ORDER BY
            "Date" DESC
        """;

        return formsJdbcTemplate.query(
                sql,
                new Object[]{projet + "%", endDateTime},
                (rs, rowNum) ->   new AvancementStat(
                        rs.getString("Date"),
                        rs.getString("Taux Avancement prévisionnel"),
                        rs.getString("Taux Avancement réel")
                ));
    }

    public AvancementStat getLatestAvancementStats(String projet, LocalDateTime endDateTime){
        String sql = """
        SELECT
            "Date",
            "Date de création"::date,
            "Taux Avancement prévisionnel",
            "Taux Avancement réel"
        FROM
            public."Fiche suivi d'avancement des travaux"
        WHERE
            projet LIKE ?
            AND "Date de création"::timestamp < ?
            AND "Date" IS NOT NULL
        ORDER BY
            "Date" DESC
            LIMIT 1
        """;

        return formsJdbcTemplate.queryForObject(
                sql,
                new Object[]{projet + "%", endDateTime},
                (rs, rowNum) ->   new AvancementStat(
                        rs.getString("Date de création"),
                        rs.getString("Taux Avancement réel"),
                        rs.getString("Taux Avancement prévisionnel")
                        ));
    }

    public List<String> findCommentsByTravaux(String projet, LocalDateTime startDateTime, LocalDateTime endDateTime ) {

        String sql = """
        SELECT "Commentaires" 
        FROM public."Fiche suivi d'avancement des travaux" 
        WHERE projet LIKE ? 
        AND "Date de création"::timestamp >= ?
        AND "Date de création"::timestamp <= ? 
        ORDER BY "Date de création" DESC
        """;

        return formsJdbcTemplate.query(
                sql,
                new Object[]{projet + "%", startDateTime, endDateTime},
                (rs, rowNum) -> rs.getString("Commentaires")
        );
    }

    public EffectifStat getProjectStatsForms(String projet, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    String sql = """
        SELECT
            COUNT(DISTINCT "Date de création"::timestamp) AS jours_travailles,
            MAX("Date de création"::date) as creationDate,
            SUM("T. Heu./J"::integer) AS total_heures,
            SUM(CASE WHEN "Date"::date BETWEEN ? AND ? THEN "T. Heu./J"::integer ELSE 0 END) AS total_heures_semaine,
            ROUND(AVG("T. Eff./J"::float)::numeric, 2) AS moyenne_par_jour,
            (
                SELECT "T. Eff./J"::integer
                FROM public."Fiche d'effectifs"
                WHERE projet LIKE ? AND "Date"::date BETWEEN ? AND ?
                ORDER BY "Date" DESC
                LIMIT 1
            ) AS effectif_a_jour,
            ROUND(SUM("T. Heu./J"::float)::numeric / NULLIF(COUNT(DISTINCT "Date de création"), 0), 2) AS moyenne_t_jours
        FROM public."Fiche d'effectifs"
        WHERE projet LIKE ? and "Date de création"::timestamp <= ?
        """;

    List<EffectifStat> results = formsJdbcTemplate.query(sql,
            new Object[]{
                    startDateTime.toLocalDate(), endDateTime.toLocalDate(),
                    projet + "%", startDateTime.toLocalDate(), endDateTime.toLocalDate(),
                    projet + "%", endDateTime
            },
            (rs, rowNum) -> {
                String creationDate = rs.getString("creationDate");
                if (creationDate == null) return null;
                return new EffectifStat(
                        LocalDate.parse(creationDate),
                        rs.getInt("jours_travailles"),
                        rs.getInt("total_heures"),
                        rs.getInt("total_heures_semaine"),
                        rs.getDouble("moyenne_t_jours"),
                        rs.getDouble("moyenne_par_jour"),
                        rs.getInt("effectif_a_jour")
                );
            });

    return results.isEmpty() || results.get(0) == null ? null : results.get(0);
}

    public EffectifStat getProjectStatsModified(String projet, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    String sql = """
        SELECT
            COUNT(DISTINCT "Date de création"::timestamp) AS jours_travailles,
            SUM("T. Heu./J"::integer) AS total_heures,
            MAX("Date de création"::date) as creationDate,
            SUM(CASE WHEN "Date"::date BETWEEN ? AND ? THEN "T. Heu./J"::integer ELSE 0 END) AS total_heures_semaine,
            ROUND(AVG("T. Eff./J"::float)::numeric, 2) AS moyenne_par_jour,
            (
                SELECT "T. Eff./J"::integer
                FROM public."Effectifs V5"
                WHERE projet LIKE ? AND "Date"::date BETWEEN ? AND ?
                ORDER BY "Date" DESC
                LIMIT 1
            ) AS effectif_a_jour,
            ROUND(SUM("T. Heu./J"::float)::numeric / NULLIF(COUNT(DISTINCT "Date de création"), 0), 2) AS moyenne_t_jours
        FROM public."Effectifs V5"
        WHERE projet LIKE ? and "Date de création"::timestamp <= ?
        """;

    List<EffectifStat> results = taskModifiedJdbcTemplate.query(sql,
            new Object[]{
                    startDateTime.toLocalDate(), endDateTime.toLocalDate(),
                    projet + "%", startDateTime.toLocalDate(), endDateTime.toLocalDate(),
                    projet + "%", endDateTime.toLocalDate()
            },
            (rs, rowNum) -> {
                String creationDate = rs.getString("creationDate");
                if (creationDate == null) return null;
                return new EffectifStat(
                        LocalDate.parse(creationDate),
                        rs.getInt("jours_travailles"),
                        rs.getInt("total_heures"),
                        rs.getInt("total_heures_semaine"),
                        rs.getDouble("moyenne_t_jours"),
                        rs.getDouble("moyenne_par_jour"),
                        rs.getInt("effectif_a_jour")
                );
            });

    return results.isEmpty() || results.get(0) == null ? null : results.get(0);
}

    public EffectifStat getCombinedProjectStats(String projet, LocalDateTime startDateTime, LocalDateTime endDateTime) {

        EffectifStat stats1 = getProjectStatsForms(projet, startDateTime, endDateTime);
        EffectifStat stats2 = getProjectStatsModified(projet, startDateTime, endDateTime);
        return EffectifStat.combine(stats1,stats2);
    }

    public FaitMarquant findFaitsMarquants(String projet, LocalDate dateStart, LocalDate dateEnd) {
        LocalDateTime startDateTime = dateStart.atStartOfDay();
        LocalDateTime endDateTime = dateEnd.atTime(LocalTime.MAX);
        String sql = """
            SELECT "Faits Marquants"
            FROM public."Fiche des faits marquants"
            WHERE projet LIKE ? 
              AND "Date de création"::date >= ?
              AND "Date de création"::date <= ?
            ORDER BY "Date de création" DESC
            """;

        List<String> faitsMarquants = formsJdbcTemplate.query(
                sql,
                new Object[]{projet + "%", startDateTime, endDateTime},
                (rs, rowNum) -> rs.getString("Faits Marquants")
        );
        return new FaitMarquant(faitsMarquants);
    }

    public ProjetDetailsDto findLatestProjetDetails(String projectPattern) {
        String sql = """

                SELECT\s
    fp."Nom du projet",
    fp."Type Projet",
    fp."Date de l’ordre du service" AS "date_ordre_service",
    fp."Date de livraison du projet",
    fp."Montant global projet en DH",
    fp."Superficie Parcelle m2",
    fp."Hauteur m2",
    fp."SHON m2",
    fp."SHOB m2",
    fp."Nombre Etages",
    fp."Nbre de clefs",
    fp."Date de modification",
    p.imageUrl
FROM\s
    public."Fiche Projet V5" fp
LEFT JOIN\s
    project p\s
    ON split_part(p.projet, ' ', 1) = split_part(fp."projet", ' ', 1)
WHERE\s
    fp."projet" LIKE ?
ORDER BY\s
    fp."Date de modification"::date DESC
LIMIT 1
""";


        return taskModifiedJdbcTemplate.queryForObject(
                sql,
                new Object[]{projectPattern},
                (rs, rowNum) -> mapRowToProjetDetailsDto(rs)
        );
    }

    private ProjetDetailsDto mapRowToProjetDetailsDto(ResultSet rs) throws SQLException {
        return new ProjetDetailsDto(
                rs.getString("Nom du projet"),
                rs.getString("Type Projet"),
                rs.getDate("date_ordre_service") != null ? rs.getDate("date_ordre_service").toLocalDate() : null,
                rs.getDate("Date de livraison du projet") != null ? rs.getDate("Date de livraison du projet").toLocalDate() : null,
                rs.getDouble("Montant global projet en DH"),
                rs.getDouble("Superficie Parcelle m2"),
                rs.getDouble("Hauteur m2"),
                rs.getDouble("SHON m2"),
                rs.getDouble("SHOB m2"),
                rs.getInt("Nombre Etages"),
                rs.getInt("Nbre de clefs"),
                rs.getDate("Date de modification") != null ? rs.getDate("Date de modification").toLocalDate() : null,
                rs.getString("imageurl")
        );
    }

    public double calculateSatisfactionPercentage(String projectPattern) {
        String sql = """
        WITH project_cr_data AS (
            SELECT 
                projet, 
                SUM(COALESCE("Cr1 - Respect des clauses du contrat, a"::int, 0) + 
                    COALESCE("Cr2 - Conformité technique, respect des"::int, 0) + 
                    COALESCE("Cr3 - Planification, Suivi, installatio"::int, 0) + 
                    COALESCE("Cr4 - Matériel, équipements, …"::int, 0) + 
                    COALESCE("Cr5 - Compétences, attitudes,….du perso"::int, 0) + 
                    COALESCE("Cr6 - Respect global des délais, délai "::int, 0) + 
                    COALESCE("Cr7 - Reporting objectif, complet et en"::int, 0) + 
                    COALESCE("Cr8 - Plan de prévention, sensibilisati"::int, 0) + 
                    COALESCE("Cr9 - Signalisation des incidents, anal"::int, 0) + 
                    COALESCE("Cr10 - Gestion des déchets chantier, in"::int, 0) + 
                    COALESCE("Cr11 - Communication avec le client, in"::int, 0) + 
                    COALESCE("Cr12 - Prise en charge des réclamations"::int, 0) + 
                    COALESCE("Cr13 - Proposition au client d'améliora"::int, 0)
                ) AS total_cr_sum,
                SUM(
                    CASE WHEN "Cr1 - Respect des clauses du contrat, a" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr2 - Conformité technique, respect des" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr3 - Planification, Suivi, installatio" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr4 - Matériel, équipements, …" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr5 - Compétences, attitudes,….du perso" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr6 - Respect global des délais, délai " IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr7 - Reporting objectif, complet et en" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr8 - Plan de prévention, sensibilisati" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr9 - Signalisation des incidents, anal" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr10 - Gestion des déchets chantier, in" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr11 - Communication avec le client, in" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr12 - Prise en charge des réclamations" IS NOT NULL THEN 1 ELSE 0 END +
                    CASE WHEN "Cr13 - Proposition au client d'améliora" IS NOT NULL THEN 1 ELSE 0 END
                ) AS cr_count
            FROM public."Fiche Satisfaction Client"
            WHERE projet like ?
            GROUP BY projet
        )
        SELECT 
            ROUND((total_cr_sum::numeric / NULLIF(cr_count, 0) * 20), 2) AS percentage
        FROM project_cr_data
        """;

        try {
            Double result = formsJdbcTemplate.queryForObject(
                    sql,
                    new Object[]{projectPattern + "%"},
                    (rs, rowNum) -> {
                        double percentage = rs.getDouble("percentage");
                        return rs.wasNull() ? 0 : percentage;
                    }
            );

            return result != null ? result : 0.0;

        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public List<String> getLatestProjectComments(String projectPattern) {
        String sql = """
        SELECT "Commentaires"
        FROM public."Fiche Projet V5"
        WHERE projet LIKE ?
        ORDER BY "Date de modification" DESC
        LIMIT 1;
        """;

        return taskModifiedJdbcTemplate.query(
                sql,
                new Object[]{projectPattern + "%"},
                (rs, rowNum) -> rs.getString("Commentaires")
        );
    }

    public List<TravauxModificatif> getTravauxModificatifList(String projectPattern) {
        String sql = """
            WITH latest_modifications AS (
                SELECT DISTINCT ON ("IssueNumber") 
                    "IssueNumber" AS issueNumber,
                    "Description de la modification" AS description,
                    "Statut FTM" AS statutFtm,
                    "Montant global" AS montantGlobal,
                    "Montant validé" AS montantValide,
                    ("Montant global"::numeric - "Montant validé"::numeric) AS difference,
                    "Date de modification" AS dateModification
                FROM public."Travaux Modificatifs V5"
                WHERE "projet" LIKE ?
                ORDER BY "IssueNumber", "Date de modification" DESC
            )
            SELECT *
            FROM latest_modifications
            WHERE description NOT ILIKE '%SANS%'
            """;

        try {
            return taskModifiedJdbcTemplate.query(sql,
                    new Object[]{projectPattern},
                    (rs, rowNum) -> new TravauxModificatif(
                            rs.getString("description"),
                            rs.getString("statutFtm"),
                            rs.getBigDecimal("montantGlobal"),
                            rs.getBigDecimal("montantValide"),
                            rs.getBigDecimal("difference")

                    )
            );
        } catch (DataAccessException e) {
            throw new RuntimeException("Error fetching modifications for project: " + projectPattern, e);
        }
    }

    public TravauxModificatifStat getTravauxModificatifsSummary(String projectPattern) {

        Double montantGlobal = findLatestProjetDetails(projectPattern).montantGlobalDh();
        // Validate input
        if (projectPattern == null || projectPattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Project pattern cannot be null or empty");
        }

        String sql = """
        WITH latest_modifications AS (
          SELECT DISTINCT ON ("IssueNumber") 
            "Description de la modification", 
            "Statut FTM", 
            "Montant global"::numeric AS montant_global, 
            "Montant validé"::numeric AS montant_valide,
            ("Montant global"::numeric - "Montant validé"::numeric) AS difference
          FROM public."Travaux Modificatifs V5"
          WHERE "projet" LIKE ?
          ORDER BY "IssueNumber", "Date de modification" DESC
        )
        SELECT 
          SUM(montant_global) AS total_montant_global,
          SUM(montant_valide) AS total_montant_valide,
          SUM(difference) AS total_difference
        FROM latest_modifications
        WHERE "Description de la modification" NOT ILIKE '%SANS%'
        """;

        try {
            return taskModifiedJdbcTemplate.queryForObject(
                    sql,
                    new Object[]{projectPattern + "%"},
                    (rs, rowNum) -> new TravauxModificatifStat(
                            rs.getBigDecimal("total_montant_global"),
                            rs.getBigDecimal("total_montant_valide"),
                            rs.getBigDecimal("total_difference"),
                            null,
                            null,
                            null,
                            BigDecimal.valueOf(montantGlobal).add(rs.getBigDecimal("total_montant_global"))

                    )
            );
        } catch (EmptyResultDataAccessException e) {
            // Return zero values if no results found
            return new TravauxModificatifStat(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
            null,
                    null,
                    null,
                    BigDecimal.ZERO);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error fetching travaux modificatifs summary for project: " + projectPattern, e);
        }
    }
}
