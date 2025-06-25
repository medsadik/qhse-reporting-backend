package org.example.qhsereportingbackend.IndicateursSST.Controller;

import org.example.qhsereportingbackend.IndicateursSST.Dto.*;
import org.example.qhsereportingbackend.IndicateursSST.Service.IndicateurSstService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/indicateursSST")
@CrossOrigin
public class IndicateurSstController {

    private final IndicateurSstService indicateurSstService;

    public IndicateurSstController(IndicateurSstService indicateurSstService) {
        this.indicateurSstService = indicateurSstService;
    }
    @GetMapping("/{projectId}/sor")
    public SorDTO getStats(@PathVariable String projectId,
                           @RequestParam LocalDate start,
                           @RequestParam LocalDate end) {
        return indicateurSstService.getStats(projectId, start, end);
    }
    @GetMapping("/{projectId}/compteRendu")
    public ReunionAndReclamationAndBonnePratiqueDTO getReclamationAndReunionAndBonnePratiquesInfos(@PathVariable String projectId,
                                                                                                   @RequestParam LocalDate start,
                                                                                                   @RequestParam LocalDate end) {
        return indicateurSstService.getReunionAndBonnePratiqueAndReclamationInfo(projectId,start,end);
    }

    @GetMapping("/{projectId}/formationAndToolbox")
    public FormationAndToolbox getFormationAndToolBox(@PathVariable String projectId,
                                        @RequestParam LocalDate start,
                                        @RequestParam LocalDate end) {

        String categorie = "SST";
        return indicateurSstService.getFormationAndToolboxStats(projectId, categorie,start, end);
    }
    @GetMapping("/{projectId}/recomponsesAndSanctions")
    public RecompenseAndSanctionDto getRecomponses(@PathVariable String projectId
                      ) {

        return indicateurSstService.getRecompenseAndSanctions(projectId);
    }

}
