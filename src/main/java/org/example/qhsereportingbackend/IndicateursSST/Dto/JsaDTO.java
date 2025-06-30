package org.example.qhsereportingbackend.IndicateursSST.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record JsaDTO(List<NameValueDto> globalStats, List<NameValueDto> globalStatsByMetier,
                     List<NameValueDto> hebdoStatsByMetier) {
}
