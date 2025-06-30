package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dao;


import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto.NameVolumeDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.example.qhsereportingbackend.utils.DynamicQuery.getNameValueDataByDynamicQuery;

@Repository
public class IndicateurEnvDao{


    private final JdbcTemplate formsJdbcTemplate;

    public IndicateurEnvDao(@Qualifier("formsJdbcTemplate") JdbcTemplate formsJdbcTemplate) {
        this.formsJdbcTemplate = formsJdbcTemplate;
    }

    public double getTotalConsumption(String projectPattern,String consumptionType){
        String sql = """
                SELECT\s
                SUM(REPLACE("%s", ',', '.')::float) AS Total
                FROM public."Fiche suivi Gestion des ressources"
                WHERE projet LIKE ?;
                """.formatted(consumptionType);
        return formsJdbcTemplate.queryForObject(sql, Double.class, projectPattern);
    }
    public List<NameValueDto> getConsumptionByYear(String projectPattern,String consumptionType){
        String sql = """
                SELECT\s
                  EXTRACT(YEAR FROM "Date"::date) AS name,
                  ROUND(
                    SUM(REPLACE("%s", ',', '.')::float)::numeric,
                    2
                  ) AS value
                FROM public."Fiche suivi Gestion des ressources"
                WHERE projet LIKE ?
                GROUP BY name
                ORDER BY name;
                """.formatted(consumptionType);
        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projectPattern},
                "name",
                "value"
        );
    }  public List<NameValueDto> getCurrentYearConsumption(String projectPattern,String consumptionType){
        String sql = """
                SELECT
                TO_CHAR("Date"::date, 'TMMonth') AS name,
                ROUND(
                  SUM(REPLACE("Consommation du carburant liquide (L)", ',', '.')::float)::numeric,
                  2
                ) AS value
              FROM public."Fiche suivi Gestion des ressources"
              WHERE projet LIKE ?
                AND EXTRACT(YEAR FROM "Date"::date) = EXTRACT(YEAR FROM CURRENT_DATE)
              GROUP BY name, EXTRACT(MONTH FROM "Date"::date)
              ORDER BY EXTRACT(MONTH FROM "Date"::date);
                
                """.formatted(consumptionType);
        return getNameValueDataByDynamicQuery(
                formsJdbcTemplate,
                sql,
                new Object[]{projectPattern},
                "name",
                "value"
        );
    }


    public List<NameVolumeDto> getWasteVolumesByType(String projetPattern) {
        String sql = """
        SELECT 
            "Type de déchet" AS name,
            SUM(REPLACE("Volume estimé traité en m3", ',', '.')::float) AS volume_traite,
            SUM(REPLACE("Volume estimé valorisé en m3", ',', '.')::float) AS volume_valorise
        FROM public."Fiche suivi déchets"
        WHERE projet LIKE ?
        GROUP BY "Type de déchet"
        ORDER BY volume_valorise DESC
    """;

        return formsJdbcTemplate.query(sql, new Object[]{projetPattern}, (rs, rowNum) -> {
            String name = rs.getString("name");
            double traite = rs.getDouble("volume_traite")/1000;
            double valorise = rs.getDouble("volume_valorise")/1000;

            Map<String, Double> volumeMap = new HashMap<>();
            volumeMap.put("traite", traite);
            volumeMap.put("valorise", valorise);

            return new NameVolumeDto(name, volumeMap);
        });
    }

    public List<NameVolumeDto> getWasteVolumesByYear(String projetPattern) {
        String sql = """
    
                SELECT\s
      EXTRACT(YEAR FROM "Date"::date) AS name,
      SUM(REPLACE("Volume estimé traité en m3", ',', '.')::float) AS volume_traite,
      SUM(REPLACE("Volume estimé valorisé en m3", ',', '.')::float) AS volume_valorise
    FROM public."Fiche suivi déchets"
    WHERE
      projet LIKE ?
    GROUP BY name
    ORDER BY name

    """;

        return formsJdbcTemplate.query(sql, new Object[]{projetPattern}, (rs, rowNum) -> {
            String name = rs.getString("name");
            double traite = rs.getDouble("volume_traite");
            double valorise = rs.getDouble("volume_valorise");

            Map<String, Double> volumeMap = new HashMap<>();
            volumeMap.put("traite", traite);
            volumeMap.put("valorise", valorise);

            return new NameVolumeDto(name, volumeMap);
        });
    }

    public String getLatestWasteDate( String projectPattern) {
        String sql = """
        SELECT "Date"
        FROM public."Fiche suivi déchets"
        WHERE projet LIKE ?
        ORDER BY "Date"::timestamp DESC
        LIMIT 1
    """;

        List<String> dates = formsJdbcTemplate.query(
                sql,
                new Object[]{projectPattern},
                (rs, rowNum) -> rs.getString("Date")
        );

        return dates.isEmpty() ? null : dates.get(0);
    }

    public Double getWasteValorizationRate( String projectPattern) {
        String sql = """
        SELECT 
            ROUND(
                (
                    (
                        SUM(REPLACE("Volume estimé valorisé en m3", ',', '.')::float) / 
                        NULLIF(SUM(REPLACE("Volume estimé traité en m3", ',', '.')::float), 0)
                    ) * 100
                )::numeric, 
                2
            ) AS taux_valorisation_pourcentage
        FROM public."Fiche suivi déchets"
        WHERE projet LIKE ?
          AND "Date" IS NOT NULL
    """;

        List<Double> results = formsJdbcTemplate.query(
                sql,
                new Object[]{projectPattern},
                (rs, rowNum) -> rs.getDouble("taux_valorisation_pourcentage")
        );

        return results.isEmpty() || results.get(0) == null ? 0 : results.get(0);
    }

}
