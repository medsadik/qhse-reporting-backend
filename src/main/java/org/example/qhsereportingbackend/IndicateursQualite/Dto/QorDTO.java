package org.example.qhsereportingbackend.IndicateursQualite.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;

import java.util.List;

public record QorDTO(
        List<NameStatusDto> globalStatutByEst,
        List<NameStatusDto> globalStatutByCategorie
) {

}