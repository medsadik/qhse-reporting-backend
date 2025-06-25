package org.example.qhsereportingbackend.IndicateursGeneraux.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TravauxModificatifStat(
        BigDecimal montantGlobal,
        BigDecimal montantValide,
        BigDecimal enCours,
        LocalDate miseAjourDate,
        BigDecimal MontantSigne,
        BigDecimal MontantReste,
        BigDecimal MontantGlobalHT
) {

}
