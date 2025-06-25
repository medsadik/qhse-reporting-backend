package org.example.qhsereportingbackend.EvaluationEST.Dao;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class EvaluationEstDao {

    public final JdbcTemplate formsJdbcTemplate;
    public final JdbcTemplate taskModifiedJdbcTemplate;


    private static final String[] EVALUATION_CRITERIA = {
            "CR - Qualité des fournitures livrées, p",
            "CR - Qualité du service offert, Perform",
            "CR - Réactivité aux réclamations",
            "CR - Disponibilité du correspondant",
            "CR - Qualifications des intervenants",
            "CR - Respect Délai de livraison",
            "CR - Qualité de la performance SST",
            "CR - Qualité de la performance Env"
    };

    private static final String EVALUATION_TABLE = "public.\"Fiche d'évaluation E-ST\"";
    private static final String REUNION_TABLE     = "public.\"Compte rendu réunion E-ST\"";
    private static final String AOR_V5_TABLE     = "public.\"AOR V5\"";


    public EvaluationEstDao(@Qualifier("formsJdbcTemplate") JdbcTemplate jdbcTemplate,@Qualifier("taskModifiedJdbcTemplate") JdbcTemplate taskModifiedJdbcTemplate) {
        this.formsJdbcTemplate = jdbcTemplate;
        this.taskModifiedJdbcTemplate = taskModifiedJdbcTemplate;
    }



    private String BuildEvaluationByLasTrimestreQuery(){
        StringBuilder scoreCalculation = new StringBuilder();
        StringBuilder countCalculation = new StringBuilder();

        // Build dynamic calculations for all criteria
        for (int i = 0; i < EVALUATION_CRITERIA.length; i++) {
            String criterion = EVALUATION_CRITERIA[i];

            if (i > 0) {
                scoreCalculation.append(" + ");
                countCalculation.append(" + ");
            }

            // Score calculation with proper null handling
            scoreCalculation.append(String.format(
                    "COALESCE(NULLIF(REPLACE(\"%s\", '%%', '')::numeric, 0), 0)",
                    criterion
            ));

            // Count calculation for non-empty values
            countCalculation.append(String.format(
                    "(CASE WHEN REPLACE(\"%s\", '%%', '') ~ '^[0-9]+(\\.[0-9]+)?$' AND REPLACE(\"%s\", '%%', '') <> '' THEN 1 ELSE 0 END)",
                    criterion, criterion
            ));
        }

        return String.format("""
        SELECT DISTINCT ON ("E-ST")
            "E-ST",
            TO_CHAR("Date d'évaluation"::date, 'YYYY') || ' T' || EXTRACT(QUARTER FROM "Date d'évaluation"::date) AS trimestre,
            ROUND(
                CASE 
                    WHEN (%s) = 0 THEN 0
                    ELSE (%s)::numeric / NULLIF((%s)::numeric, 0)
                END,
                2
            ) AS score_evaluation
        FROM %s
        WHERE projet LIKE ?
            AND "E-ST" IS NOT NULL
            AND "Date d'évaluation" IS NOT NULL
            AND "Date d'évaluation" = (
                SELECT MAX("Date d'évaluation")
                FROM %s
                WHERE projet LIKE ?
                    AND "Date d'évaluation" IS NOT NULL
            )
        """,
                countCalculation,
                scoreCalculation,
                countCalculation,
                EVALUATION_TABLE,
                EVALUATION_TABLE
        );
    }
    public List<NameValueDto> findEvaluationsByLastTrimestreTypeSafe(String projectId) {
        String sql = BuildEvaluationByLasTrimestreQuery();
        String projectPattern = projectId + "%";

        return formsJdbcTemplate.query(sql, new Object[]{projectPattern, projectPattern},
                (rs, rowNum) -> {
                    Double score = rs.getDouble("score_evaluation");
                    return new NameValueDto(
                            rs.getString("E-ST"),
                            score != null ? score : 0
                    );
                }
        );
    }

    public int findTotalPvs(String projectPattern) {
        String sql = String.format(
                "SELECT COUNT(*) as total_pv FROM %s WHERE projet LIKE ?", REUNION_TABLE
        );

        return formsJdbcTemplate.query(sql, new Object[]{projectPattern}, rs -> {
            if (rs.next()) {
                int result = rs.getInt("total_pv");
                return result;
            } else {
                return 0; // Aucun résultat
            }
        });
    }

    public int findTotalActions(String projectPattern){
        String sql = String.format(
                "SELECT COUNT(DISTINCT \"IssueNumber\") as \"totalActions\" " +
                        "FROM  %s" +
                        "WHERE projet LIKE ? " +
                        "AND \"Type d'audit\" = 'Evaluation E-ST'; ",AOR_V5_TABLE
        );
        return taskModifiedJdbcTemplate.query(sql, new Object[]{projectPattern}, rs -> {
            if (rs.next()) {
                int result = rs.getInt("totalActions");
                return result;
            } else {
                return 0; // ou null selon ta logique métier
            }
        });    }

    public List<NameValueDto> findDetailReunions(String projectPattern) {
        String groupByColumn = "\"E-ST\"";
        String sql = String.format(
                "SELECT %s, COUNT(*) as totalPvGroupedByST FROM %s WHERE projet LIKE ? GROUP BY %s",
                groupByColumn,
                REUNION_TABLE,
                groupByColumn
        );

        return formsJdbcTemplate.query(sql, new Object[]{projectPattern}, (rs, rowNum) -> {
            NameValueDto reunionDetail = new NameValueDto(rs.getString("E-ST"),rs.getInt("totalPvGroupedByST"));
            return reunionDetail;
        });
    }

    public List<NameValueDto> findDetailActions(String projectPattern){
        String sql = """
        WITH grouped_data AS (
            SELECT DISTINCT ON (id)
                "E-ST",
                CONCAT(
                    EXTRACT(YEAR FROM "Date"::DATE)::TEXT,
                    ' ',
                    CASE
                        WHEN EXTRACT(MONTH FROM "Date"::DATE) BETWEEN 1 AND 3 THEN 'T1'
                        WHEN EXTRACT(MONTH FROM "Date"::DATE) BETWEEN 4 AND 6 THEN 'T2'
                        WHEN EXTRACT(MONTH FROM "Date"::DATE) BETWEEN 7 AND 9 THEN 'T3'
                        WHEN EXTRACT(MONTH FROM "Date"::DATE) BETWEEN 10 AND 12 THEN 'T4'
                        ELSE 'Unknown'
                    END
                ) AS trimestre
            FROM public."AOR V5"
            WHERE projet LIKE ?
              AND "Type d'audit" = 'Evaluation E-ST'
            ORDER BY id, "Date" DESC
        ),
        latest_trimester AS (
            SELECT MAX(trimestre) AS max_trimestre
            FROM grouped_data
        )
        SELECT
            gd."E-ST",
            gd.trimestre,
            COUNT(*) AS nbprojet
        FROM grouped_data gd
        JOIN latest_trimester lt ON gd.trimestre = lt.max_trimestre
        GROUP BY gd."E-ST", gd.trimestre
        ORDER BY gd.trimestre DESC
        """;
        return taskModifiedJdbcTemplate.query(sql,new Object[]{projectPattern},((rs, rowNum) -> {
            Map<String,String> result = new HashMap<>();
            NameValueDto actionDetail = new NameValueDto(rs.getString("E-ST"),rs.getInt("nbprojet"));
            result.put("E-ST", rs.getString("E-ST"));
            result.put("nbProjet", rs.getString("nbprojet"));
            return actionDetail;
        }));
    }


    public List<Map<String,String>> findReactiviteByActions(String projectId){
        return null;
    }


//    public List<ActionStatus> findDetailsActionsByEst(String projectPattern) {
//        String sql = """
//        WITH ranked_issues AS (
//            SELECT
//                "IssueNumber",
//                "E-ST",
//                "S0",
//                "Date de création",
//                "Date de modification",
//                ROW_NUMBER() OVER (
//                    PARTITION BY "IssueNumber", "E-ST"
//                    ORDER BY "Date de modification" DESC
//                ) AS rn
//            FROM public."AOR V5"
//            WHERE projet LIKE ?
//              AND "Type d'audit" = 'Evaluation E-ST'
//        )
//        SELECT
//            sub."E-ST",
//            sub."Status Analysis",
//            COUNT(*) AS "Count"
//        FROM (
//            SELECT
//                "IssueNumber",
//                "E-ST",
//                "S0",
//                CASE
//                    WHEN "S0" = 'Approuvé, clôturé' OR "S0" = 'Approuvé, Suivi' THEN 'Approuvé, clôturé'
//                    WHEN "S0" = 'En cours' AND (CURRENT_DATE - ("Date de création"::date)) > 30 THEN '+ un mois'
//                    WHEN "S0" = 'En cours' THEN 'En cours'
//                    WHEN "S0" = 'Signalé prêt' THEN 'Signalé prêt'
//                    WHEN "S0" = 'Sans suite' THEN 'Sans suite'
//                    ELSE 'Unknown status'
//                END AS "Status Analysis"
//            FROM ranked_issues
//            WHERE rn = 1
//        ) sub
//        GROUP BY
//            sub."E-ST",
//            sub."Status Analysis"
//        ORDER BY
//            sub."E-ST",
//            sub."Status Analysis";
//    """;
//        return taskModifiedJdbcTemplate.query(sql,new Object[]{projectPattern+"%"}, rs -> {
//            Map<String, List<Status>> tempMap = new HashMap<>();
//
//            while (rs.next()) {
//                String est = rs.getString("E-ST");
//                String status = rs.getString("Status Analysis");
//                int count = rs.getInt("Count");
//
//                tempMap.computeIfAbsent(est, k -> new ArrayList<>())
//                        .add(new Status(status, count));
//            }
//
//            // Convert the temporary map to a list of ActionStatus records
//            return tempMap.entrySet().stream()
//                    .map(entry -> new ActionStatus(entry.getKey(), entry.getValue()))
//                    .collect(Collectors.toList());
//        });
////        return taskModifiedJdbcTemplate.query(sql, rs -> {
////            Map<String, Map<String, Integer>> result = new HashMap<>();
////
////            while (rs.next()) {
////                String est = rs.getString("E-ST");
////                String status = rs.getString("Status Analysis");
////                int count = rs.getInt("Count");
////
////                result.computeIfAbsent(est, k -> new HashMap<>()).put(status, count);
////            }
////
////            return result;
////        });
//    }
    public List<NameStatusDto> findDetailsActionsByEst(String projectPattern) {
    String sql = """
        WITH ranked_issues AS (
            SELECT
                "IssueNumber",
                "E-ST",
                "S0",
                "Date de création",
                "Date de modification",
                ROW_NUMBER() OVER (
                    PARTITION BY "IssueNumber", "E-ST"
                    ORDER BY "Date de modification" DESC
                ) AS rn
            FROM public."AOR V5"
            WHERE projet LIKE ?
              AND "Type d'audit" = 'Evaluation E-ST'
        )
        SELECT
            sub."E-ST",
            sub."Status Analysis",
            COUNT(*) AS "Count"
        FROM (
            SELECT
                "IssueNumber",
                "E-ST",
                "S0",
                CASE
                    WHEN "S0" = 'Approuvé, clôturé' OR "S0" = 'Approuvé, Suivi' THEN 'Approuvé, clôturé'
                    WHEN "S0" = 'En cours' AND (CURRENT_DATE - ("Date de création"::date)) > 30 THEN '+ un mois'
                    WHEN "S0" = 'En cours' THEN 'En cours'
                    WHEN "S0" = 'Signalé prêt' THEN 'Signalé prêt'
                    WHEN "S0" = 'Sans suite' THEN 'Sans suite'
                    ELSE 'Unknown status'
                END AS "Status Analysis"
            FROM ranked_issues
            WHERE rn = 1
        ) sub
        GROUP BY
            sub."E-ST",
            sub."Status Analysis"
        ORDER BY
            sub."E-ST",
            sub."Status Analysis";
    """;

    return taskModifiedJdbcTemplate.query(sql, new Object[]{projectPattern + "%"}, rs -> {
        Map<String, Map<String, Integer>> grouped = new LinkedHashMap<>();

        while (rs.next()) {
            String est = rs.getString("E-ST");
            String status = rs.getString("Status Analysis");
            int count = rs.getInt("Count");

            grouped.computeIfAbsent(est, k -> new LinkedHashMap<>())
                    .put(status, count);
        }

        return grouped.entrySet().stream()
                .map(entry -> new NameStatusDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    });
}

    public List<NameStatusDto> actionStatusByLatestTrimestre(String projectPattern) {
        String sql = """
        WITH sub AS (
            SELECT DISTINCT ON ("IssueNumber", "E-ST")
                "IssueNumber",
                "E-ST",
                "S0",
                CASE
                    WHEN "S0" = 'Approuvé, clôturé' OR "S0" = 'Approuvé, Suivi' THEN 'Approuvé, clôturé'
                    WHEN "S0" = 'En cours' AND (CURRENT_DATE - ("Date de création"::date)) > 30 THEN '+ un mois'
                    WHEN "S0" = 'En cours' THEN 'En cours'
                    WHEN "S0" = 'Signalé prêt' THEN 'Signalé prêt'
                    WHEN "S0" = 'Sans suite' THEN 'Sans suite'
                    ELSE 'Unknown status'
                END AS "Status Analysis",
                CONCAT('T', EXTRACT(QUARTER FROM "Date"::date), ' ', EXTRACT(YEAR FROM "Date"::date)) AS "Trimestre Année",
                EXTRACT(YEAR FROM "Date"::date) AS year_val,
                EXTRACT(QUARTER FROM "Date"::date) AS quarter_val
            FROM public."AOR V5"
            WHERE projet LIKE ?
              AND "Type d'audit" = 'Evaluation E-ST'
            ORDER BY "IssueNumber", "E-ST", "Date de modification" DESC
        ),
        last_quarter AS (
            SELECT year_val, quarter_val
            FROM sub
            ORDER BY year_val DESC, quarter_val DESC
            LIMIT 1
        )
        SELECT 
            sub."E-ST",
            sub."Status Analysis",
            COUNT(*) AS "Count",
            sub."Trimestre Année"
        FROM sub
        JOIN last_quarter ON sub.year_val = last_quarter.year_val 
                         AND sub.quarter_val = last_quarter.quarter_val
        GROUP BY sub."E-ST", sub."Status Analysis", sub."Trimestre Année"
        ORDER BY sub."E-ST", sub."Status Analysis";
    """;

        return taskModifiedJdbcTemplate.query(sql,
                ps -> ps.setString(1, projectPattern + "%"),
                rs -> {
                    Map<String, Map<String, Integer>> grouped = new LinkedHashMap<>();

                    while (rs.next()) {
                        String est = rs.getString("E-ST");
                        String status = rs.getString("Status Analysis");
                        int count = rs.getInt("Count");

                        grouped.computeIfAbsent(est, k -> new LinkedHashMap<>())
                                .put(status, count);
                    }

                    return grouped.entrySet().stream()
                            .map(entry -> new NameStatusDto(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                }
        );
    }

    public List<NameValueDto> calculateReactiviteByEst(String projectPattern) {
        String sql = """
        WITH latest_per_id AS (
            SELECT DISTINCT ON ("id") 
                "id", 
                "E-ST", 
                "Date de création", 
                "Date de modification"
            FROM public."AOR V5"
            WHERE projet LIKE ? 
              AND "Type d'audit" = 'Evaluation E-ST'
            ORDER BY "id", "Date de modification" DESC
        )
        SELECT
            "E-ST",
            ROUND(AVG("Date de modification"::date - "Date de création"::date)) AS Reactivite
        FROM latest_per_id
        GROUP BY "E-ST"
        ORDER BY Reactivite DESC;
    """;

        return taskModifiedJdbcTemplate.query(sql,
                ps -> ps.setString(1, projectPattern+"%"),
                (rs, rowNum) -> new NameValueDto(
                        rs.getString("E-ST"),
                        rs.getInt("Reactivite")
                )
        );
    }

    public int getGlobalAverageReactivity(String projectPattern) {
        String sql = """
        WITH latest_per_id AS (
            SELECT DISTINCT ON ("id") 
                "id", 
                "E-ST", 
                "Date de création", 
                "Date de modification"
            FROM public."AOR V5"
            WHERE projet LIKE ? 
              AND "Type d'audit" = 'Evaluation E-ST'
            ORDER BY "id", "Date de modification" DESC
        ),
        reactivite_par_est AS (
            SELECT
                "E-ST",
                ROUND(AVG("Date de modification"::date - "Date de création"::date)::numeric) AS Reactivite
            FROM latest_per_id
            GROUP BY "E-ST"
        )
        SELECT ROUND(AVG(Reactivite)::numeric) AS Moyenne_Reactivite_Globale
        FROM reactivite_par_est;
    """;

        return taskModifiedJdbcTemplate.queryForObject(
                sql,
                new Object[]{projectPattern},
                Integer.class
        );
    }

    public int getGlobalAverageReactivityByLastTrimestre(String projectPattern) {
        String sql = """
        WITH latest_per_id AS (
            SELECT DISTINCT ON ("id") 
                "id", 
                "Date de création", 
                "Date de modification", 
                TO_CHAR("Date"::date, 'YYYY') || ' T' || EXTRACT(QUARTER FROM "Date"::date) AS trimestre
            FROM public."AOR V5"
            WHERE projet LIKE ? 
              AND "Type d'audit" = 'Evaluation E-ST'
            ORDER BY "id", "Date de modification" DESC
        )
        SELECT 
            ROUND(AVG("Date de modification"::date - "Date de création"::date)) AS Reactivite
        FROM latest_per_id
        GROUP BY trimestre
        ORDER BY trimestre DESC 
        LIMIT 1;
    """;

        return taskModifiedJdbcTemplate.queryForObject(
                sql,
                new Object[]{projectPattern},
                Integer.class
        );
    }

}
