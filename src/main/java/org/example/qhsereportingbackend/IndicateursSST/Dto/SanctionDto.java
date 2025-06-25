package org.example.qhsereportingbackend.IndicateursSST.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameObjectDto;

import java.util.ArrayList;
import java.util.List;

public record SanctionDto(
        String lastDate,
        String totalSanctions,
        List<NameObjectDto> decisions
) {

    public static SanctionDto sanctionDto(List<NameObjectDto> input) {
        String maxDate = null;
        String totalRecompenses = null;
        List<NameObjectDto> decisions = new ArrayList<>();

        for (NameObjectDto item : input) {
            String name = item.name();
            String value = item.value();
            if ("Max Date de création".equalsIgnoreCase(name)) {
                maxDate = String.valueOf(value);
            } else if ("Total Sanctions".equalsIgnoreCase(name)) {
                totalRecompenses = String.valueOf(value);
            } else if (name.startsWith("Décision")) {
                String cleanedName = name.replaceFirst("Décision:\\s*", "").trim();
                decisions.add(new NameObjectDto(cleanedName, value));
            }
        }
        return new SanctionDto(maxDate, totalRecompenses, decisions);
    }
}
