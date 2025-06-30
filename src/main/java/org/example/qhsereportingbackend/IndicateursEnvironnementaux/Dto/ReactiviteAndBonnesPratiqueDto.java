package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.example.qhsereportingbackend.IndicateursSST.Dto.BonnesPratiquesStatsDto;

import java.util.List;

public record ReactiviteAndBonnesPratiqueDto(List<NameValueDto> reactiviteByCategories, List<NameValueDto> reactiviteByEst,
                                             List<NameValueDto> reactiviteByHierarchieDeControle, BonnesPratiquesStatsDto bonnesPratiques) {
}
