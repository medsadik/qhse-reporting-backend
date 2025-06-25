package org.example.qhsereportingbackend.IndicateursQualite.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record GeneralQualityDto (
    List<NameValueDto> generalStats,
    List<NameValueDto> tauxConformite,
    List<NameValueDto> totalStatutByControlType,
    List<NameValueDto> hebdoStatutByControlType
    ){
}
