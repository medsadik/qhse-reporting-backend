package org.example.qhsereportingbackend.IndicateursGeneraux.Controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.qhsereportingbackend.IndicateursGeneraux.Dto.*;
import org.example.qhsereportingbackend.IndicateursGeneraux.Service.IndicateursGenerauxService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/indicateursGeneraux")
@CrossOrigin
@Tag(name = "Indicateurs Généraux", description = "Endpoints pour les indicateurs généraux du projet : Effectif, Avancement des travaux et les faits marquants")
public class IndicateursGeneralController {

    private final IndicateursGenerauxService indicateursGenerauxService;

    public IndicateursGeneralController(IndicateursGenerauxService indicateursGenerauxService) {
        this.indicateursGenerauxService = indicateursGenerauxService;
    }

    @GetMapping("/{projectId}/effectifInfo")
    @Operation(
            summary = "Récupérer l'effectif",
            description = "Retourne l'effectif pour un projet donné entre deux dates"
    )
    public Effectif getEffectifByDate(
            @Parameter(description = "ID du projet") @PathVariable String projectId,
            @Parameter(description = "Date de début au format ISO (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Date de fin au format ISO (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return indicateursGenerauxService.getEffectifInfos(projectId, start, end);
    }

    @GetMapping("/{projectId}/avancementTravaux")
    @Operation(
            summary = "Récupérer l'avancement des travaux",
            description = "Retourne les informations d'avancement pour un projet donné entre deux dates"
    )
    public AvancementTravaux getAvancementTravaux(
            @Parameter(description = "ID du projet") @PathVariable String projectId,
            @Parameter(description = "Date de début au format ISO (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Date de fin au format ISO (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return indicateursGenerauxService.getAvancementTravaux(projectId, start, end);
    }

    @GetMapping("/{projectId}/faitsMarquants")
    @Operation(
            summary = "Récupérer les faits marquants",
            description = "Retourne les faits marquants pour un projet donné entre deux dates"
    )
    public FaitMarquant getFaitsMarquants(
            @Parameter(description = "ID du projet") @PathVariable String projectId,
            @Parameter(description = "Date de début au format ISO (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Date de fin au format ISO (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return indicateursGenerauxService.getFaitsMarquant(projectId, start, end);
    }

    @GetMapping("/{projectId}/projectDetails")
    public GlobalProjectDetails getProjetDetails(@PathVariable String projectId) {
//        log.info("getProjetDetails for "+projectId);
        return indicateursGenerauxService.getProjetDetailsDto(projectId);
    }
    @GetMapping("/{projectId}/travauxModificatif")
    public List<TravauxModificatif> getTravauxModificatif(@PathVariable String projectId) {
        return indicateursGenerauxService.getTravauxModificatifList(projectId);
    }
    @GetMapping("/{projectId}/travauxModificatif/montant")
    public TravauxModificatifStat getTravauxModificatifStat(@PathVariable String projectId) {
        return indicateursGenerauxService.getTravauxModificatifStat(projectId);
    }

}

