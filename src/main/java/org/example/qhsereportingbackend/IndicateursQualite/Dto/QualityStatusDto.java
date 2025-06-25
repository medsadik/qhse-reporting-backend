package org.example.qhsereportingbackend.IndicateursQualite.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

import static org.example.qhsereportingbackend.EvaluationEST.Service.EvaluationEstService.calculateAverageScore;

public record QualityStatusDto(
        List<NameValueDto> globalStatus,
        List<NameStatusDto> globalStatusByFamille,
        List<NameStatusDto> hebdoStatusByFamille,
        List<NameValueDto> reactivityByFamille,
        double reactiviteGlobale
) {
    public QualityStatusDto(
            List<NameValueDto> globalStatus,
            List<NameStatusDto> globalStatusByFamille,
            List<NameStatusDto> hebdoStatusByFamille,
            List<NameValueDto> reactivityByFamille
    ) {
        this(globalStatus, globalStatusByFamille, hebdoStatusByFamille, reactivityByFamille,
                calculateAverageScore(reactivityByFamille));
    }
}
