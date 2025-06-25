package org.example.qhsereportingbackend.IndicateursSST.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record ReunionAndReclamationAndBonnePratiqueDTO(double nbReclamations , List<NameValueDto> reclamationInfo, List<NameValueDto> reunionInfo,
                                                       BonnesPratiquesStatsDto bonnesPratiquesInfo) {
}
