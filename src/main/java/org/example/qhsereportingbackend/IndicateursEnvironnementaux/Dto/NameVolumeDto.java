package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto;

import java.util.Map;

public record NameVolumeDto(
        String name,
        Map<String, Double> volume
) {
}