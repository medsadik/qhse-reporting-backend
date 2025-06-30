package org.example.qhsereportingbackend.IndicateursSST.Service;

import org.example.qhsereportingbackend.GlobalDto.NameObjectDto;
import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.example.qhsereportingbackend.GlobalRepository.GlobalRepository;
import org.example.qhsereportingbackend.GlobalServices.GeneralServices;
import org.example.qhsereportingbackend.IndicateursSST.Dto.*;
import org.example.qhsereportingbackend.IndicateursSST.Dao.IndicateurSstDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.qhsereportingbackend.IndicateursSST.Dto.RecompenseDto.recompenseDto;
import static org.example.qhsereportingbackend.IndicateursSST.Dto.SanctionDto.sanctionDto;
import static org.example.qhsereportingbackend.utils.DynamicQuery.addAverageScore;
import static org.example.qhsereportingbackend.utils.TablesConstant.tables;

@Service
public class IndicateurSstService {


    private final GlobalRepository globalRepository;
    public final String tableName = "SOR V5";
    private final IndicateurSstDao indicateurSstDao;
    private final JdbcTemplate formsJdbcTemplate;
    private final JdbcTemplate taskModifiedJdbcTemplate;
    private final GeneralServices generalServices;

    public IndicateurSstService(GlobalRepository globalRepository, IndicateurSstDao indicateurSstDao, JdbcTemplate formsJdbcTemplate, JdbcTemplate taskModifiedJdbcTemplate, GeneralServices generalServices) {
        this.globalRepository = globalRepository;
        this.indicateurSstDao = indicateurSstDao;
        this.formsJdbcTemplate = formsJdbcTemplate;
        this.taskModifiedJdbcTemplate = taskModifiedJdbcTemplate;
        this.generalServices = generalServices;
    }

    public SorDTO getStats(String projetId, LocalDate startDate, LocalDate endDate) {
        String projetPattern = projetId + "%";
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<NameValueDto> tableStats = globalRepository.getTableStats(tableName, projetPattern, startDateTime, endDateTime, taskModifiedJdbcTemplate);
        List<NameValueDto> statutBySD = globalRepository.getStatutBySD(tableName, projetPattern);
        List<NameValueDto> globalStatut = globalRepository.getGlobalStatut(tableName, projetPattern);
        List<NameStatusDto> globalStatuByCategorie = globalRepository.getGlobalStatuByColumn(tableName, projetPattern, "Catégories",taskModifiedJdbcTemplate);
        List<NameStatusDto> globalStatuByEst = globalRepository.getGlobalStatuByColumn(tableName, projetPattern, "ST / Société",taskModifiedJdbcTemplate);
        List<NameStatusDto> hebdoStatuByCategorie = globalRepository.getHebdoStatuByColumn(tableName, projetPattern, "Catégories", startDateTime, endDateTime, taskModifiedJdbcTemplate);
        List<NameStatusDto> hebdoStatuByEst = globalRepository.getHebdoStatuByColumn(tableName, projetPattern, "ST / Société", startDateTime, endDateTime, taskModifiedJdbcTemplate);
        List<NameValueDto> reactiviteByCategorie = globalRepository.getReactiviteByTable(tableName, projetPattern, "Catégories");
        List<NameValueDto> reactiviteByEst = globalRepository.getReactiviteByTable(tableName, projetPattern, "ST / Société");
        List<NameValueDto> reactiviteByHierachieControle = globalRepository.getReactiviteByTable(tableName, projetPattern, "Hiérarchie du contrôle");
        addAverageScore(reactiviteByCategorie,"Total");
        addAverageScore(reactiviteByEst,"Total");
        addAverageScore(reactiviteByHierachieControle,"Total");
        return new SorDTO(tableStats,globalStatut,statutBySD,globalStatuByCategorie,globalStatuByEst,hebdoStatuByCategorie,hebdoStatuByEst,reactiviteByCategorie,reactiviteByEst,reactiviteByHierachieControle);
    }

    public ReunionAndReclamationAndBonnePratiqueDTO getReunionAndBonnePratiqueAndReclamationInfo(String projetId, LocalDate startDate, LocalDate endDate){
        String projetPattern = projetId + "%";
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        String type = "SST";
        List<NameValueDto> reclamation = getReclamation(projetPattern);
        return new ReunionAndReclamationAndBonnePratiqueDTO(reclamation.stream().mapToDouble(NameValueDto::value).sum(),reclamation,
                getReunionInfo(projetPattern,startDateTime,endDateTime),
                getBonnePratiquesStats(projetPattern,type));
    }

    public BonnesPratiquesStatsDto getBonnePratiquesStats(String projetId,String type) {
        String projetPattern = projetId + "%";
        return globalRepository.getBonnesPratiquesStats(projetPattern,type);
    }

    public List<NameValueDto> getReunionInfo(String projetPattern, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return indicateurSstDao.getReunionInfo(projetPattern,startDateTime,endDateTime);
    }
    public List<NameValueDto> getReclamation(String projetPattern) {
        List<NameValueDto> reclamationInfo = indicateurSstDao.getReclamationInfo(projetPattern);
        return reclamationInfo;
    }

//    public FormationOrToolBoxDto getFormationOrToolboxStats(String tableName,String categorie, String projetId, LocalDate startDate, LocalDate endDate) {
//        String projetPattern = projetId + "%";
//        LocalDateTime startDateTime = startDate.atStartOfDay();
//        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
//        List<NameObjectDto> formationStats = indicateurSstDao.getFormationOrToolBoxStats(tableName, projetPattern,categorie, startDateTime, endDateTime);
//        List<NameValueDto> totalThematiques = indicateurSstDao.getTotalThematiques(tableName, projetPattern,categorie);
//        List<NameValueDto> hebdoThematiques = indicateurSstDao.getHebdoThematiques(tableName, projetPattern, startDateTime, endDateTime,categorie);
//        List<NameObjectDto> globalStats = new ArrayList<>();
//        List<NameObjectDto> hebdoStats = new ArrayList<>();
//        String lastDate = null;
//        for (NameObjectDto item : formationStats) {
//             String name = item.name();
//            if ("Dernière Date".equalsIgnoreCase(name)) {
//                lastDate = String.valueOf(item.value());
//            } else if (name.startsWith("IH")) {
//                hebdoStats.add(item);
//            } else {
//                globalStats.add(item);
//            }
//        }
//        return new FormationOrToolBoxDto(globalStats,hebdoStats,lastDate,totalThematiques,hebdoThematiques);
//    }

    public FormationAndToolbox getFormationAndToolboxStats(String projetId,LocalDate startDate, LocalDate endDate) {
        String categorie = "SST";
        FormationOrToolBoxDto formations = generalServices.getFormationOrToolboxStats("Formations",categorie, projetId ,startDate, endDate);
        FormationOrToolBoxDto toolbox = generalServices.getFormationOrToolboxStats("Toolbox",categorie, projetId ,startDate, endDate);

        return new FormationAndToolbox(formations,toolbox);

    }

    public RecompenseDto getRecompenses(String projetId) {
        String projetPattern = projetId + "%";
        List<NameObjectDto> recompenses = indicateurSstDao.getRecompenses(projetPattern);
        return recompenseDto(recompenses);
    }
    public SanctionDto getSanctions(String projetId) {
        String projetPattern = projetId + "%";
        List<NameObjectDto> recompenses = indicateurSstDao.getSanctions(projetPattern);
        return sanctionDto(recompenses);
    }

    public RecompenseAndSanctionDto getRecompenseAndSanctions(String projetId) {
        return new RecompenseAndSanctionDto(getRecompenses(projetId),getSanctions(projetId));
    }

    public SuDTO getSuInfo(String projetId, LocalDate startDate, LocalDate endDate) {
        String projetPattern = projetId + "%";
        String groupedColumnEST = "E-ST";
        String countColumnEST = "\"E-ST\"";
        String groupedColumnTheme = "Thème de la simulation";
        String countColumnTheme = "\"Thème de la simulation\"";
        List<NameObjectDto> su = indicateurSstDao.getSuInfo(projetPattern);
        List<NameObjectDto> globalStats = new ArrayList<>();
        String simulationDate = null;
        String lastTheme = null;

        for (NameObjectDto item : su) {
            String name = item.name();
            if ("Date Simulation".equalsIgnoreCase(name)) {
                simulationDate = String.valueOf(item.value());
            } else if ("Thème Simulation".equalsIgnoreCase(name)) {
                lastTheme = String.valueOf(item.value());
            } else {
                globalStats.add(item);
            }
        }
        List<NameValueDto> globalStatsByEntreprise = indicateurSstDao.getGlobalTableInfoGrouped(tables.get("Su"), groupedColumnEST, countColumnEST, projetPattern);
        List<NameValueDto> globalStatsByTheme = indicateurSstDao.getGlobalTableInfoGrouped(tables.get("Su"), groupedColumnTheme, countColumnTheme, projetPattern);
        List<NameValueDto> MensualStatsByTheme = indicateurSstDao.getMensuelTableInfoGrouped(groupedColumnTheme, countColumnTheme,projetPattern,startDate,endDate);
        List<NameValueDto> MensualStatsByEntreprise = indicateurSstDao.getMensuelTableInfoGrouped(groupedColumnEST, countColumnEST,projetPattern,startDate,endDate);
        return new SuDTO(globalStats,simulationDate,lastTheme,globalStatsByEntreprise,globalStatsByTheme,MensualStatsByEntreprise,MensualStatsByTheme);
    }

    public JsaDTO getJsaInfo(String projetId, LocalDate startDate, LocalDate endDate) {
        String projetPattern = projetId + "%";
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        String columnToGroupBy = "Métiers";
        String columnToCountBy = "\"Métiers\"";

        String jsa = tables.get("Jsa");
        List<NameValueDto> globalStats = globalRepository.getTableStats(jsa, projetPattern, startDateTime, endDateTime,formsJdbcTemplate);
        List<NameValueDto> globalStatsByMetier = indicateurSstDao.getGlobalTableInfoGrouped(jsa, columnToGroupBy,columnToCountBy, projetPattern);
        List<NameValueDto> hebdoStatsByMetier = indicateurSstDao.getHebdoTableInfoGrouped(jsa, columnToGroupBy,columnToCountBy, projetPattern, startDateTime, endDateTime);
        return new JsaDTO(globalStats,globalStatsByMetier,hebdoStatsByMetier);
    }

    public InductionDto getInductionInfo(String projetId, LocalDate startDate, LocalDate endDate) {
        String projetPattern = projetId + "%";
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        String induction = tables.get("Induction");
        String columnToGroupBy = "Entreprise";
        String columnToCountBy = "\"Entreprise\"";

        List<NameValueDto> globalStats = globalRepository.getTableStats(induction, projetPattern, startDateTime, endDateTime,formsJdbcTemplate);
        List<NameValueDto> globalStatsByEst = indicateurSstDao.getGlobalTableInfoGrouped(induction, columnToGroupBy,columnToCountBy, projetPattern);
        List<NameValueDto> hebdoStatsByEst = indicateurSstDao.getHebdoTableInfoGrouped(induction, columnToGroupBy,columnToCountBy, projetPattern, startDateTime, endDateTime);

        return new InductionDto(globalStats,globalStatsByEst,hebdoStatsByEst);

    }

    public RecrutementDto getRecrutementInfo(String projetId, LocalDate startDate, LocalDate endDate) {
        String projetPattern = projetId + "%";
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        String recrutement = tables.get("Recrutement");
        String columnToGroupBy = "E-ST";

        List<NameValueDto> globalStats = globalRepository.getTableStats(recrutement, projetPattern, startDateTime, endDateTime,formsJdbcTemplate);
        List<NameValueDto> globalStatsByEst = indicateurSstDao.getGlobalTableInfoGrouped(recrutement, columnToGroupBy,"Distinct id", projetPattern);
        List<NameValueDto> hebdoStatsByEst = indicateurSstDao.getHebdoTableInfoGrouped(recrutement, columnToGroupBy,"Distinct id", projetPattern, startDateTime, endDateTime);

        globalStats.add(indicateurSstDao.getTotalRecrutementLocal(projetPattern));
        globalStats.add(indicateurSstDao.getTotalRecrutementLocalADate(projetPattern,startDate,endDate));
        return new RecrutementDto(globalStats,globalStatsByEst,hebdoStatsByEst);

    }

    public ParcDto getParcInfo(String projetId){
        String projetPattern = projetId + "%";
        List<NameValueDto> parcStats = indicateurSstDao.getParcStats(projetPattern);
        List<NameValueDto> parcTypologies = indicateurSstDao.getParcTypologies(projetPattern);
        return new ParcDto(parcStats,parcTypologies);
    }
}
