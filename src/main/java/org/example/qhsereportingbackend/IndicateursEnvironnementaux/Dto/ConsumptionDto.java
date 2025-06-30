package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record ConsumptionDto(
        double total,
        List<NameValueDto> yearConsumption,
        List<NameValueDto> currentyearConsumption
) {
}
