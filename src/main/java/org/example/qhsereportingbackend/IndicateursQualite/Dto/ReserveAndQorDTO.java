package org.example.qhsereportingbackend.IndicateursQualite.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

import static org.example.qhsereportingbackend.EvaluationEST.Service.EvaluationEstService.calculateAverageScore;

public record ReserveAndQorDTO(
        List<NameValueDto> generalStats,
        List<NameValueDto> globalStatut,
        List<NameStatusDto> globalStatutByMetier,
        List<NameStatusDto> hebdoStatutByMetier,
        List<NameValueDto> reactiviteByMetier,
        double reactiviteGlobale

){
    public ReserveAndQorDTO(
            List<NameValueDto> generalStats,
            List<NameValueDto> globalStatut,
            List<NameStatusDto> globalStatutByMetier,
            List<NameStatusDto> hebdoStatutByMetier,
            List<NameValueDto> reactiviteByMetier
    )
        {
            this(generalStats, globalStatut, globalStatutByMetier, hebdoStatutByMetier,reactiviteByMetier,
                    calculateAverageScore(reactiviteByMetier));
        }

}
