package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Service;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.example.qhsereportingbackend.GlobalRepository.GlobalRepository;
import org.example.qhsereportingbackend.GlobalServices.GeneralServices;
import org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dao.IndicateurEnvDao;
import org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto.*;
import org.example.qhsereportingbackend.IndicateursSST.Dto.BonnesPratiquesStatsDto;
import org.example.qhsereportingbackend.IndicateursSST.Dto.FormationAndToolbox;
import org.example.qhsereportingbackend.IndicateursSST.Dto.FormationOrToolBoxDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.qhsereportingbackend.utils.DynamicQuery.addAverageScore;
import static org.example.qhsereportingbackend.utils.TablesConstant.tables;

@Service
public class IndicateursEnvService {

    public static final String EST = "ST / Société";
    public static final String CATEGORIES = "Catégories";
    private static final String EOR = tables.get("Eor");
    private final GlobalRepository globalRepository;
    private final JdbcTemplate taskModifiedJdbcTemplate;
    private final GeneralServices generalServices;
    private final IndicateurEnvDao indicateurEnvDao;

    public IndicateursEnvService(GlobalRepository globalRepository, JdbcTemplate taskModifiedJdbcTemplate, GeneralServices generalServices, IndicateurEnvDao indicateurEnvDao) {
        this.globalRepository = globalRepository;
        this.taskModifiedJdbcTemplate = taskModifiedJdbcTemplate;
        this.generalServices = generalServices;
        this.indicateurEnvDao = indicateurEnvDao;
    }


    public CategorieEorDTO getEorStatusWithCategories(String projectId, LocalDate start, LocalDate end){
        String projetPattern = projectId + "%";
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<NameValueDto> tableStats = globalRepository.getTableStats(EOR, projetPattern, startDateTime, endDateTime, taskModifiedJdbcTemplate);
        List<NameStatusDto> globalStatutByCategorie = globalRepository.getGlobalStatuByColumn(EOR, projetPattern, CATEGORIES, taskModifiedJdbcTemplate);
        List<NameStatusDto> hebdoStatutByCategorie = globalRepository.getHebdoStatuByColumn(EOR, projetPattern, CATEGORIES, startDateTime, endDateTime, taskModifiedJdbcTemplate);
        List<NameValueDto> globalStatut = globalRepository.getGlobalStatut(EOR, projetPattern);
        return new CategorieEorDTO(tableStats,globalStatut,globalStatutByCategorie,hebdoStatutByCategorie);
    }

    public EstEorDto getEorStatusWithST(String projectId, LocalDate start, LocalDate end) {
        String projetPattern = projectId + "%";
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        List<NameValueDto> statutBySD = globalRepository.getStatutBySD(EOR, projetPattern);
        List<NameStatusDto> globalStatutByEst = globalRepository.getGlobalStatuByColumn(EOR, projetPattern, EST, taskModifiedJdbcTemplate);
        List<NameStatusDto> hebdoStatutByEst = globalRepository.getHebdoStatuByColumn(EOR, projetPattern, EST, startDateTime, endDateTime, taskModifiedJdbcTemplate);

        return new EstEorDto(statutBySD, globalStatutByEst, hebdoStatutByEst);

    }


    public ReactiviteEor getReactiviteEor(String projectId){
        String projetPattern = projectId + "%";
        List<NameValueDto> reactiviteByCategories = globalRepository.getReactiviteByTable(EOR, projetPattern, CATEGORIES);
        addAverageScore(reactiviteByCategories,"Total");
        List<NameValueDto> reactiviteByEst = globalRepository.getReactiviteByTable(EOR, projetPattern, EST);
        addAverageScore(reactiviteByEst,"Total");
        List<NameValueDto> reactiviteByHierarchieDeControle = globalRepository.getReactiviteByTable(EOR, projetPattern, "Hiérarchie du contrôle");
        addAverageScore(reactiviteByHierarchieDeControle,"Total");

        return new ReactiviteEor(reactiviteByCategories,reactiviteByEst,reactiviteByHierarchieDeControle);
    }

    public BonnesPratiquesStatsDto getBonnePratiquesStats(String projectId) {
        String projetPattern = projectId + "%";
        String type = "Environnement";
        return globalRepository.getBonnesPratiquesStats(projetPattern,type);
    }

    public FormationAndToolbox getFormationAndToolboxStats(String projetId, String categorie, LocalDate startDate, LocalDate endDate) {
        FormationOrToolBoxDto formations = generalServices.getFormationOrToolboxStats("Formations", categorie, projetId ,startDate, endDate);
        FormationOrToolBoxDto toolbox = generalServices.getFormationOrToolboxStats("Toolbox", categorie, projetId ,startDate, endDate);

        return new FormationAndToolbox(formations,toolbox);

    }
    public ConsumptionDto getConsumptionStatByType(String projetPattern, String consumptionType) {
        List<NameValueDto> consumptionByYear = indicateurEnvDao.getConsumptionByYear(projetPattern, consumptionType);
        List<NameValueDto> CurrentYearconsumption = indicateurEnvDao.getCurrentYearConsumption(projetPattern, consumptionType);
        double totalConsumption = indicateurEnvDao.getTotalConsumption(projetPattern, consumptionType);
        return new ConsumptionDto(totalConsumption,consumptionByYear,CurrentYearconsumption);
    }


    public Map<String, ConsumptionDto> getConsumptionStats(String projectId) {
        String projetPattern = projectId + "%";

        Map<String, String> consommationCarburant = Map.of(
                "consommationCarburant", "Consommation du carburant liquide (L)",
                "consommationEau", "Consommation de l'eau (m3)",
                "consommationElectricite", "Consommation électricité (KWT)"
        );

        return consommationCarburant.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> getConsumptionStatByType(projetPattern, entry.getValue())
                ));
    }

    public WasteDto getWasteInfo(String projetId) {
        String projetPattern = projetId + "%";
        String latestWasteDate = indicateurEnvDao.getLatestWasteDate(projetPattern);
        Double wasteValorizationRate = indicateurEnvDao.getWasteValorizationRate(projetPattern);
        List<NameVolumeDto> wasteVolumesByType = indicateurEnvDao.getWasteVolumesByType(projetPattern);
        List<NameVolumeDto> wasteVolumesByYear = indicateurEnvDao.getWasteVolumesByYear(projetPattern);
        return new WasteDto(latestWasteDate,wasteVolumesByType,wasteVolumesByYear,wasteValorizationRate);
    }

}
