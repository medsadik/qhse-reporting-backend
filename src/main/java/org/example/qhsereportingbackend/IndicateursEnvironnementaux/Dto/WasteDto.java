package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto;

import java.util.List;

public record WasteDto(String latestWasteDate, List<NameVolumeDto> wasteVolumesByType,
                       List<NameVolumeDto> wasteVolumesByYear, Double wasteValorizationRate) {
}
