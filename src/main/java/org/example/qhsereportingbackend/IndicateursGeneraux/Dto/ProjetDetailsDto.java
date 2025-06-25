package org.example.qhsereportingbackend.IndicateursGeneraux.Dto;

import java.time.LocalDate;

public record ProjetDetailsDto(
        String nomDuProjet,
        String typeProjet,
        LocalDate dateOrdreService,
        LocalDate dateLivraison,
        Double montantGlobalDh,
        Double superficieParcelleM2,
        Double hauteurM2,
        Double shonM2,
        Double shobM2,
        Integer nombreEtages,
        Integer nombreClefs,
        LocalDate dateModification,
        String imageUrl
) {}
