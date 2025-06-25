package org.example.qhsereportingbackend.EvaluationEST.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;

import java.util.List;

public record EvaluationStatus(List<NameStatusDto> globalActionStatus,
                               List<NameStatusDto> actionStatusByTrimestre,
                               List<org.example.qhsereportingbackend.GlobalDto.NameValueDto> reactiviteByEst,
                               int avgReactivite,
                               int lastTrimestreAvgReactivite
                               ) {
}
