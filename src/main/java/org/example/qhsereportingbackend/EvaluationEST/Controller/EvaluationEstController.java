package org.example.qhsereportingbackend.EvaluationEST.Controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.qhsereportingbackend.EvaluationEST.Dto.*;
import org.example.qhsereportingbackend.EvaluationEST.Service.EvaluationEstService;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evaluationEst")
@CrossOrigin
@Tag(name = "Évaluation E-ST", description = "Endpoints pour la gestion des évaluations E-ST")
public class EvaluationEstController {

    private final EvaluationEstService evaluationEstService;

    public EvaluationEstController(EvaluationEstService evaluationEstService) {
        this.evaluationEstService = evaluationEstService;
    }

    @Operation(
            summary = "Obtenir les détails d'évaluation",
            description = "Récupère les détails complets d'une évaluation E-ST pour un projet donné"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détails d'évaluation trouvés",
                    content = @Content(schema = @Schema(implementation = EvaluationDetails.class))),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("{projectId}/evaluationDetails")
    public EvaluationDetails getEvaluationDetails(
            @Parameter(description = "ID du projet", required = true, example = "CNGR123")
            @PathVariable String projectId) {
        return evaluationEstService.getEvaluationDetails(projectId);
    }

    @Operation(
            summary = "Obtenir les évaluations par dernier trimestre",
            description = "Récupère les données d'évaluation E-ST pour le dernier trimestre disponible"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Données trimestrielles trouvées",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EvaluationTrimestre.class)))),
            @ApiResponse(responseCode = "404", description = "Aucune donnée disponible"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("{projectId}/evaluationByLastTrimestre")
    public List<NameValueDto> getEvaluationByLastTrimestre(
            @Parameter(description = "ID du projet", required = true, example = "HTM456")
            @PathVariable String projectId) {
        return evaluationEstService.getEvaluationByLastTrimestre(projectId);
    }

    @Operation(
            summary = "Obtenir le statut d'évaluation",
            description = "Récupère le statut actuel d'une évaluation E-ST"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut d'évaluation trouvé",
                    content = @Content(schema = @Schema(implementation = EvaluationStatus.class))),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/{projectId}/status")
    public EvaluationStatus getEvaluationStatus(
            @Parameter(description = "ID du projet", required = true, example = "APRM789")
            @PathVariable String projectId) {
        return evaluationEstService.getEvaluationStatus(projectId);
    }

    @GetMapping("/{projectId}/evaluationStats")
    public EvaluationStats getGlobalEvaluationDetails(@PathVariable String projectId){
        return evaluationEstService.getEvaluationStats(projectId);
    }


}
