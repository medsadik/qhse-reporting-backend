package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Controller;


import org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto.*;
import org.example.qhsereportingbackend.IndicateursEnvironnementaux.Service.IndicateursEnvService;
import org.example.qhsereportingbackend.IndicateursSST.Dto.BonnesPratiquesStatsDto;
import org.example.qhsereportingbackend.IndicateursSST.Dto.FormationAndToolbox;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/indicateursEnv")
@CrossOrigin
public class IndicateursEnvController{

    private final IndicateursEnvService indicateursEnvService;

    public IndicateursEnvController(IndicateursEnvService indicateursEnvService) {
        this.indicateursEnvService = indicateursEnvService;
    }

    @GetMapping("/{projectId}/eor/statusByCategories")
    public CategorieEorDTO getStatusEorByCategories(@PathVariable String projectId,
                                       @RequestParam LocalDate start,
                                       @RequestParam LocalDate end) {
        return indicateursEnvService.getEorStatusWithCategories(projectId,start,end);
    }
    @GetMapping("/{projectId}/eor/statusByEst")
    public EstEorDto getStatusEorByEst(@PathVariable String projectId,
                                       @RequestParam LocalDate start,
                                       @RequestParam LocalDate end) {
        return indicateursEnvService.getEorStatusWithST(projectId,start,end);
    }
    @GetMapping("/{projectId}/eor/reactivite")
    public ReactiviteEor getReactivite(@PathVariable String projectId
                                       ) {
        return indicateursEnvService.getReactiviteEor(projectId);
    }
    @GetMapping("/{projectId}/eor/bonnesPratiques")
    public BonnesPratiquesStatsDto getBonnesPratiques(@PathVariable String projectId
                                       ) {
        return indicateursEnvService.getBonnePratiquesStats(projectId);
    }
    @GetMapping("/{projectId}/eor/formationAndToolbox")
    public FormationAndToolbox getEnvFormationAndToolBox(@PathVariable String projectId,
                                                      @RequestParam String categorie,
                                                      @RequestParam LocalDate start,
                                                      @RequestParam LocalDate end) {
        return indicateursEnvService.getFormationAndToolboxStats(projectId,categorie,start, end);
    }

    @GetMapping("/{projectId}/consumption")
    public Map<String, ConsumptionDto> getConsumptionInfo(@PathVariable String projectId){
        return indicateursEnvService.getConsumptionStats(projectId);
    }
    @GetMapping("/{projectId}/waste")
    public WasteDto getWasteInfo(@PathVariable String projectId){
        return indicateursEnvService.getWasteInfo(projectId);
    }
}
