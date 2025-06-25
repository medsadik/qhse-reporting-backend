package org.example.qhsereportingbackend.IndicateursSST.Dto;

public record BonnesPratiquesStatsDto(int totalCount, java.time.LocalDate latestCreationDate,
                                      java.util.List<String> bonnesPratiques) {
}
