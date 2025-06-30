package org.example.qhsereportingbackend.IndicateursSST.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record ParcDto(List<NameValueDto> globalStats, List<NameValueDto> parcTypologies){
}
