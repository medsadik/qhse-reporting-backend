package org.example.qhsereportingbackend.IndicateursQualite.Controller;

import org.example.qhsereportingbackend.IndicateursQualite.Dto.*;
import org.example.qhsereportingbackend.IndicateursQualite.Service.IndicateurQualiteService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/indicateurQualite")
@CrossOrigin
public class IndicateurQualiteController {

    private final IndicateurQualiteService indicateurQualiteService;

    public IndicateurQualiteController(IndicateurQualiteService indicateurQualiteService) {
        this.indicateurQualiteService = indicateurQualiteService;
    }

    @GetMapping("/{projectId}/generalQualityStats")
    public GeneralQualityDto getQualityStats(
            @PathVariable String projectId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        return indicateurQualiteService.getGenralQuality(projectId, start, end);
    }

    @GetMapping("/{projectId}/statsByFamille")
    public QualityStatusDto getTestQualityStats2(
            @PathVariable String projectId,
            @RequestParam String conformite,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
            ){
        return indicateurQualiteService.getQualityStatsByConformityAndFamille(projectId,conformite,start,end);
    }

    @GetMapping("/{projectId}/statsByTable")
    public ReserveAndQorDTO getReactivite(
            @RequestParam String table,
            @PathVariable String projectId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
            ){
        return indicateurQualiteService.getTableStat(table,projectId,start,end);
    }

    @GetMapping("/{projectId}/additionalStats")
    public QorDTO getReactivite(
            @PathVariable String projectId
            ){
        return indicateurQualiteService.getAdditinalStats(projectId);
    }
}