package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record CategorieEorDTO(List<NameValueDto> tableStats, List<NameValueDto> globalStatut,
                              List<NameStatusDto> globalStatutByCategorie,
                              List<NameStatusDto> hebdoStatutByCategorie) {
}
