package org.example.qhsereportingbackend.IndicateursSST.Service;

import org.example.qhsereportingbackend.GlobalDto.NameObjectDto;
import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.example.qhsereportingbackend.GlobalRepository.GlobalRepository;
import org.example.qhsereportingbackend.IndicateursSST.Dto.*;
import org.example.qhsereportingbackend.IndicateursSST.Dao.IndicateurSstDao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.qhsereportingbackend.IndicateursSST.Dto.RecompenseDto.recompenseDto;
import static org.example.qhsereportingbackend.IndicateursSST.Dto.SanctionDto.sanctionDto;
import static org.example.qhsereportingbackend.utils.DynamicQuery.addAverageScore;

@Service
public class IndicateurSstService {


    private final GlobalRepository globalRepository;
    public final String tableName = "\"SOR V5\"";
    private final IndicateurSstDao indicateurSstDao;
    public IndicateurSstService(GlobalRepository globalRepository, IndicateurSstDao indicateurSstDao) {
        this.globalRepository = globalRepository;
        this.indicateurSstDao = indicateurSstDao;
    }

    public SorDTO getStats(String projetId, LocalDate startDate, LocalDate endDate) {
        String projetPattern = projetId + "%";
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<NameValueDto> tableStats = globalRepository.getTableStats(tableName, projetPattern, startDateTime, endDateTime);
        List<NameValueDto> globalStatut = globalRepository.getGlobalStatut(tableName, projetPattern);
        List<NameStatusDto> globalStatuByCategorie = globalRepository.getGlobalStatuByColumn(tableName, projetPattern, "Catégories");
        List<NameStatusDto> globalStatuByEst = globalRepository.getGlobalStatuByColumn(tableName, projetPattern, "ST / Société");
        List<NameStatusDto> hebdoStatuByCategorie = globalRepository.getHebdoStatuByColumn(tableName, projetPattern, "Catégories", startDateTime, endDateTime);
        List<NameStatusDto> hebdoStatuByEst = globalRepository.getHebdoStatuByColumn(tableName, projetPattern, "ST / Société", startDateTime, endDateTime);
        List<NameValueDto> reactiviteByCategorie = globalRepository.getReactiviteByTable(tableName, projetPattern, "Catégories");
        List<NameValueDto> reactiviteByEst = globalRepository.getReactiviteByTable(tableName, projetPattern, "ST / Société");
        List<NameValueDto> reactiviteByHierachieControle = globalRepository.getReactiviteByTable(tableName, projetPattern, "Hiérarchie du contrôle");
        addAverageScore(reactiviteByCategorie,"Total");
        addAverageScore(reactiviteByEst,"Total");
        addAverageScore(reactiviteByHierachieControle,"Total");
        return new SorDTO(tableStats,globalStatut,globalStatuByCategorie,globalStatuByEst,hebdoStatuByCategorie,hebdoStatuByEst,reactiviteByCategorie,reactiviteByEst,reactiviteByHierachieControle);
    }

    public ReunionAndReclamationAndBonnePratiqueDTO getReunionAndBonnePratiqueAndReclamationInfo(String projetId, LocalDate startDate, LocalDate endDate){
        String projetPattern = projetId + "%";
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<NameValueDto> reclamation = getReclamation(projetPattern);
        return new ReunionAndReclamationAndBonnePratiqueDTO(reclamation.stream().mapToDouble(NameValueDto::value).sum(),reclamation,
                getReunionInfo(projetPattern,startDateTime,endDateTime),
                getBonnePratiquesStats(projetPattern));
    }

    public BonnesPratiquesStatsDto getBonnePratiquesStats(String projetId) {
        String projetPattern = projetId + "%";
        return indicateurSstDao.getBonnesPratiquesStats(projetPattern);
    }

    public List<NameValueDto> getReunionInfo(String projetPattern, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return indicateurSstDao.getReunionInfo(projetPattern,startDateTime,endDateTime);
    }
    public List<NameValueDto> getReclamation(String projetPattern) {
        List<NameValueDto> reclamationInfo = indicateurSstDao.getReclamationInfo(projetPattern);
        return reclamationInfo;
    }

    public FormationOrToolBoxDto getFormationOrToolboxStats(String tableName,String categorie, String projetId, LocalDate startDate, LocalDate endDate) {
        String projetPattern = projetId + "%";
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<NameObjectDto> formationStats = indicateurSstDao.getFormationOrToolBoxStats(tableName, projetPattern,categorie, startDateTime, endDateTime);
        List<NameValueDto> totalThematiques = indicateurSstDao.getTotalThematiques(tableName, projetPattern,categorie);
        List<NameValueDto> hebdoThematiques = indicateurSstDao.getHebdoThematiques(tableName, projetPattern, startDateTime, endDateTime,categorie);
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

    public FormationAndToolbox getFormationAndToolboxStats(String projetId,String categorie, LocalDate startDate, LocalDate endDate) {
        FormationOrToolBoxDto formations = getFormationOrToolboxStats("Formations",categorie, projetId ,startDate, endDate);
        FormationOrToolBoxDto toolbox = getFormationOrToolboxStats("Toolbox",categorie, projetId ,startDate, endDate);

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
}
