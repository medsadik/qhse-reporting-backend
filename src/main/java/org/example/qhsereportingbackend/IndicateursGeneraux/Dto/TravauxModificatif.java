package org.example.qhsereportingbackend.IndicateursGeneraux.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TravauxModificatif(
        String description,
        String statutFtm,
        BigDecimal montantGlobal,
        BigDecimal montantValide,
        BigDecimal enCours
) {}