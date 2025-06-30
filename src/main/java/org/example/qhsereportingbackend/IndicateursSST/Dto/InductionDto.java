package org.example.qhsereportingbackend.IndicateursSST.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record InductionDto(List<NameValueDto> globalStats, List<NameValueDto> globalStatsByEst,
                           List<NameValueDto> hebdoStatsByEst) {
}
