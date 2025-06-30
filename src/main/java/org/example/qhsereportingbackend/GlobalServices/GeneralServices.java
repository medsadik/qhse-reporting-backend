package org.example.qhsereportingbackend.GlobalServices;

import org.example.qhsereportingbackend.GlobalDto.NameObjectDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.example.qhsereportingbackend.GlobalRepository.GlobalRepository;
import org.example.qhsereportingbackend.IndicateursSST.Dto.FormationOrToolBoxDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeneralServices {


    private final GlobalRepository globalRepository;

    public GeneralServices(GlobalRepository globalRepository) {
        this.globalRepository = globalRepository;
    }

    public FormationOrToolBoxDto getFormationOrToolboxStats(String tableName, String categorie, String projetId, LocalDate startDate, LocalDate endDate) {
        String projetPattern = projetId + "%";
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<NameObjectDto> formationStats = globalRepository.getFormationOrToolBoxStats(tableName, projetPattern,categorie, startDateTime, endDateTime);
        List<NameValueDto> totalThematiques = globalRepository.getTotalThematiques(tableName, projetPattern,categorie);
        List<NameValueDto> hebdoThematiques = globalRepository.getHebdoThematiques(tableName, projetPattern, startDateTime, endDateTime,categorie);
        List<NameObjectDto> globalStats = new ArrayList<>();
        List<NameObjectDto> hebdoStats = new ArrayList<>();
        String lastDate = null;
        for (NameObjectDto item : formationStats) {
            String name = item.name();
            if ("Dernière Date".equalsIgnoreCase(name)) {
                lastDate = String.valueOf(item.value());
            } else if (name.startsWith("IH")) {
                hebdoStats.add(item);
            } else {
                globalStats.add(item);
            }
        }
        return new FormationOrToolBoxDto(globalStats,hebdoStats,lastDate,totalThematiques,hebdoThematiques);
    }
}
