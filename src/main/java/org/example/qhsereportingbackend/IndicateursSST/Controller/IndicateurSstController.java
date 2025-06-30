package org.example.qhsereportingbackend.IndicateursSST.Controller;

import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.example.qhsereportingbackend.IndicateursSST.Dto.*;
import org.example.qhsereportingbackend.IndicateursSST.Service.IndicateurSstService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

        return indicateurSstService.getFormationAndToolboxStats(projectId,start, end);
    }
    @GetMapping("/{projectId}/recomponsesAndSanctions")
    public RecompenseAndSanctionDto getRecomponses(@PathVariable String projectId
                      ) {

        return indicateurSstService.getRecompenseAndSanctions(projectId);
    }
    @GetMapping("/{projectId}/su")
    public SuDTO getSu(@PathVariable String projectId,
                       @RequestParam LocalDate start,
                       @RequestParam LocalDate end)
                       {

        return indicateurSstService.getSuInfo(projectId, start, end);
    }
    @GetMapping("/{projectId}/jsa")
    public JsaDTO getJsa(@PathVariable String projectId,
                       @RequestParam LocalDate start,
                       @RequestParam LocalDate end)
                       {

        return indicateurSstService.getJsaInfo(projectId, start, end);
    }
    @GetMapping("/{projectId}/induction")
    public InductionDto getInduction(@PathVariable String projectId,
                       @RequestParam LocalDate start,
                       @RequestParam LocalDate end)
                       {

        return indicateurSstService.getInductionInfo(projectId, start, end);
    }
    @GetMapping("/{projectId}/recrutement")
    public RecrutementDto getRecrutement(@PathVariable String projectId,
                                         @RequestParam LocalDate start,
                                         @RequestParam LocalDate end)
                       {

        return indicateurSstService.getRecrutementInfo(projectId, start, end);
    }
    @GetMapping("/{projectId}/parc")
    public ParcDto getParcInfo(@PathVariable String projectId) {
        return indicateurSstService.getParcInfo(projectId);
    }

}
