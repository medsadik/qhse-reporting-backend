package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record EstEorDto(List<NameValueDto> statutBySD, List<NameStatusDto> globalStatutByEst,
                        List<NameStatusDto> hebdoStatutByEst) {
}
