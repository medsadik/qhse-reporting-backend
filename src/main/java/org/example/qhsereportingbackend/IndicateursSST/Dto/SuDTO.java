package org.example.qhsereportingbackend.IndicateursSST.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameObjectDto;

import java.util.List;

public record SuDTO(List<NameObjectDto> globalStats, String simulationDate, String lastTheme,
                    List<org.example.qhsereportingbackend.GlobalDto.NameValueDto> globalStatsByEntreprise,
                    List<org.example.qhsereportingbackend.GlobalDto.NameValueDto> globalStatsByTheme,
                    List<org.example.qhsereportingbackend.GlobalDto.NameValueDto> mensualStatsByEntreprise,
                    List<org.example.qhsereportingbackend.GlobalDto.NameValueDto> mensualStatsByTheme) {
}
