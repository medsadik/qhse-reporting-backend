package org.example.qhsereportingbackend.IndicateursQualite.Service;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.example.qhsereportingbackend.IndicateursQualite.Dao.IndicateurQualiteDao;
import org.example.qhsereportingbackend.IndicateursQualite.Dto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class IndicateurQualiteService {

    private final IndicateurQualiteDao indicateurQualiteDao;
    private final Map<String,String> tables = Map.of("RSV","\"Réserves OPR V5\"","QOR","\"QOR V5\"","SOR","\"SOR V5\"");
    public IndicateurQualiteService(IndicateurQualiteDao indicateurQualiteDao) {
        this.indicateurQualiteDao = indicateurQualiteDao;
    }

    public List<NameValueDto> getQualityStats(String projectPattern, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<String> conformityStatuses = Arrays.asList(null, "Conforme", "Non Conforme");
        List<NameValueDto> nameValueDtos = new ArrayList<>();
        Map<String, Integer> totalQuality = indicateurQualiteDao.getTotalQuality(projectPattern);
        Map<String, Integer> totalQualityHebdo = indicateurQualiteDao.getTotalQualityHebdo(projectPattern, startDateTime, endDateTime);
        Map<String, Integer> totalQualityHebdoClosed = indicateurQualiteDao.getTotalQualityHebdoClosed(projectPattern,startDateTime, endDateTime);

        for(Map.Entry<String, Integer> entry : totalQuality.entrySet()) {
            if (entry.getKey().equals("Total")) {
                nameValueDtos.add(new NameValueDto("Nb AC", entry.getValue()));
            }
            if (entry.getKey().equals("Conforme")) {
                nameValueDtos.add(new NameValueDto("Nb AC Conforme", entry.getValue()));
            }
            if (entry.getKey().equals("Non Conforme")) {
                nameValueDtos.add(new NameValueDto("Nb AC Non Conforme", entry.getValue()));
            }
        }
        for(Map.Entry<String, Integer> entry : totalQualityHebdo.entrySet()) {
            if (entry.getKey().equals("Total")) {
                nameValueDtos.add(new NameValueDto("IH - AC", entry.getValue()));
            }
            if (entry.getKey().equals("Conforme")) {
                nameValueDtos.add(new NameValueDto("IH -Conforme", entry.getValue()));
            }
            if (entry.getKey().equals("Non Conforme")) {
                nameValueDtos.add(new NameValueDto("IH - Non Conforme", entry.getValue()));
            }        }
        for(Map.Entry<String, Integer> entry : totalQualityHebdoClosed.entrySet()) {
            if (entry.getKey().equals("Total")) {
                nameValueDtos.add(new NameValueDto("IH - Cloture AC", entry.getValue()));
            }
            if (entry.getKey().equals("Conforme")) {
                nameValueDtos.add(new NameValueDto("IH - Cloture AC Conforme", entry.getValue()));
            }
            if (entry.getKey().equals("Non Conforme")) {
                nameValueDtos.add(new NameValueDto("IH - Cloture AC Non Conforme", entry.getValue()));
            }        }
        return nameValueDtos;

    }

    public GeneralQualityDto getGenralQuality(String projetId, LocalDate start, LocalDate end) {
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(LocalTime.MAX);
        String projetPattern = projetId + "%";
        List<NameValueDto> hebdoConformiteByControlType = getHebdoConformiteByControlType(projetPattern, startDate, endDate);
        List<NameValueDto> conformiteByControlType = getConformiteByControlType(projetPattern);
        List<NameValueDto> tauxConformite = getTauxConformite(projetPattern);
        List<NameValueDto> qualityStats = getQualityStats(projetPattern, startDate, endDate);
        return new GeneralQualityDto(qualityStats,tauxConformite,conformiteByControlType,hebdoConformiteByControlType);
    }

    public List<NameValueDto> getTauxConformite(String projectPattern) {
        return indicateurQualiteDao.getConformiteStats(projectPattern);
    }
    public List<NameValueDto> getConformiteByControlType(String projectPattern) {

        return indicateurQualiteDao.getACByControlType(projectPattern);
    }
    public List<NameValueDto> getHebdoConformiteByControlType(String projectPattern,LocalDateTime startDate, LocalDateTime endDate) {
        return indicateurQualiteDao.getHebdoACByControlType(projectPattern,startDate,endDate);
    }
    public QualityStatusDto getQualityStatsByConformityAndFamille(String projetId, String conformityStatus, LocalDate start, LocalDate end) {
        if(conformityStatus.equals("NonConforme")) {
            conformityStatus = "Non Conforme";
        }

        List<NameStatusDto> clotureStatutByFamille = getClotureStatutByFamille(projetId, conformityStatus);
        List<NameValueDto> clotureStatut = getClotureStatut(projetId, conformityStatus);
        List<NameStatusDto> hebdoClotureStatutByFamille = getHebdoClotureStatutByFamille(projetId, conformityStatus, start, end);
        List<NameValueDto> reactivityByFamille = getReactivityByFamille(projetId, conformityStatus);
        return new QualityStatusDto(clotureStatut,clotureStatutByFamille,hebdoClotureStatutByFamille,reactivityByFamille);
    }

    public List<NameValueDto> getClotureStatut(String projetId, String conformityStatus) {
        String projetPattern = projetId + "%";
        return indicateurQualiteDao.getClotureStatut(projetPattern,conformityStatus);
    }

    public List<NameStatusDto> getHebdoClotureStatutByFamille(String projetId, String conformityStatus, LocalDate start, LocalDate end) {
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(LocalTime.MAX);
        String projetPattern = projetId + "%";
        return indicateurQualiteDao.getHebdoCloturedByFamille(projetPattern,conformityStatus,startDate,endDate);
    }
    public List<NameStatusDto> getClotureStatutByFamille(String projetId, String conformityStatus) {
        String projetPattern = projetId + "%";
        return indicateurQualiteDao.getCloturedByFamille(projetPattern,conformityStatus);
    }
    public List<NameValueDto> getReactivityByFamille(String projetId, String conformityStatus) {
        String projetPattern = projetId + "%";
        return indicateurQualiteDao.getReactivityByFamille(projetPattern,conformityStatus);
    }


    public List<NameStatusDto> getRerserveState(String table, String projectId) {
        String projetPattern = projectId + "%";
        return indicateurQualiteDao.getGlobalStatuByColumn(tables.get(table),projetPattern, "QOR Concerne");
    }

     public List<NameStatusDto> getTableHebdoState(String table, String projectId, LocalDate start, LocalDate end) {
         LocalDateTime startDate = start.atStartOfDay();
         LocalDateTime endDate = end.atTime(LocalTime.MAX);
         String projetPattern = projectId + "%";

         return indicateurQualiteDao.getHebdoStatusByTableAndMetiers(tables.get(table),projetPattern,startDate,endDate);
     }

     public List<NameValueDto> getReactiviteByTable(String table, String projectId) {
        String projetPattern = projectId + "%";
         String columnName = "Métiers";
         return indicateurQualiteDao.getReactiviteByTable(tables.get(table),projetPattern, columnName);
     }


    public List<NameValueDto> getReservationStats(String table, String projectId, LocalDate start, LocalDate end) {
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(LocalTime.MAX);
        String projetPattern = projectId + "%";
        return indicateurQualiteDao.getTableStats(tables.get(table),projetPattern,startDate,endDate);
    }

    public ReserveAndQorDTO getReserveStat(String table, String projectId, LocalDate start, LocalDate end) {
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(LocalTime.MAX);
        String projetPattern = projectId + "%";
        String columnName = "Métiers";

        List<NameValueDto> tableStats = indicateurQualiteDao.getTableStats(tables.get(table), projetPattern, startDate, endDate);
        List<NameValueDto> globalStatut = indicateurQualiteDao.getGlobalStatut(tables.get(table), projetPattern);
        List<NameStatusDto> globalStatusByTableAndMetiers = indicateurQualiteDao.getGlobalStatuByColumn(tables.get(table), projetPattern, columnName);
        List<NameStatusDto> hebdoStatusByTableAndMetiers = indicateurQualiteDao.getHebdoStatuByColumn(tables.get(table), projetPattern, columnName,startDate,endDate);
        List<NameValueDto> reactiviteByTable = indicateurQualiteDao.getReactiviteByTable(tables.get(table), projetPattern, columnName);

        return new ReserveAndQorDTO(tableStats,globalStatut,globalStatusByTableAndMetiers,hebdoStatusByTableAndMetiers,reactiviteByTable);

    }

    public ReserveAndQorDTO getQorStat(String table, String projectId, LocalDate start, LocalDate end) {
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(LocalTime.MAX);
        String projetPattern = projectId + "%";
        String metierColumn = "Métiers";

        List<NameValueDto> tableStats = indicateurQualiteDao.getTableStats(tables.get(table), projetPattern, startDate, endDate);
        List<NameValueDto> globalStatut = indicateurQualiteDao.getGlobalStatut(tables.get(table), projetPattern);
        List<NameStatusDto> globalStatusByTableAndMetiers = indicateurQualiteDao.getGlobalStatuByColumn(tables.get(table), projetPattern, metierColumn);
        List<NameStatusDto> hebdoStatusByTableAndMetiers = indicateurQualiteDao.getHebdoStatuByColumn(tables.get(table), projetPattern, metierColumn,startDate,endDate);
        List<NameValueDto> reactiviteByTable = indicateurQualiteDao.getReactiviteByTable(tables.get(table), projetPattern, metierColumn);

        return new ReserveAndQorDTO(tableStats,globalStatut,globalStatusByTableAndMetiers,hebdoStatusByTableAndMetiers,reactiviteByTable);

    }

    public QorDTO getAdditinalStats(String projectId) {
        String projetPattern = projectId + "%";
        List<NameStatusDto> globalStatuByColumn = indicateurQualiteDao.getGlobalStatuByColumn(tables.get("QOR"), projetPattern, "E-ST");
        List<NameStatusDto> globalStatuByColumn1 = indicateurQualiteDao.getGlobalStatuByColumn(tables.get("QOR"), projetPattern, "QOR Concerne");
        return new QorDTO(globalStatuByColumn,globalStatuByColumn1);

    }
    public ReserveAndQorDTO getTableStat(String table, String projectId, LocalDate start, LocalDate end) {
        if (table.equals("RSV")) {
            return getReserveStat(table, projectId, start, end);
        } else if (table.equals("QOR")) {
            return getQorStat(table, projectId, start, end);
        }
        return null;
    }


}
