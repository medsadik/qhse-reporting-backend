package org.example.qhsereportingbackend.EvaluationEST.Service;

import org.example.qhsereportingbackend.EvaluationEST.Dao.EvaluationEstDao;
import org.example.qhsereportingbackend.EvaluationEST.Dto.*;
import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class EvaluationEstService {


    private final EvaluationEstDao evaluationEstDao;

    public EvaluationEstService(EvaluationEstDao evaluationEstDao) {
        this.evaluationEstDao = evaluationEstDao;
    }

    public EvaluationDetails getEvaluationDetails(String projectId) {
        String projectPattern = projectId + "%";
        List<NameValueDto> detailReunions = evaluationEstDao.findDetailReunions(projectPattern);
        int totalPvs = evaluationEstDao.findTotalPvs(projectPattern);
        int totalActions = evaluationEstDao.findTotalActions(projectPattern);
        List<NameValueDto> detailActions = evaluationEstDao.findDetailActions(projectPattern);

        return new EvaluationDetails(detailActions, detailReunions, totalPvs, totalActions);


    }

    public EvaluationStats getEvaluationStats(String projectId) {
        String projectPattern = projectId + "%";
        EvaluationDetails evaluationDetails = getEvaluationDetails(projectPattern);
        List<NameValueDto> evaluationByLastTrimestre = getEvaluationByLastTrimestre(projectPattern);
        addAverageScore(evaluationByLastTrimestre, "total");
        return new EvaluationStats(evaluationDetails, evaluationByLastTrimestre);
    }

    private static void addAverageScore(List<NameValueDto> nameValueMap, String name) {
        double avgRounded = calculateAverageScore(nameValueMap);
        NameValueDto total = new NameValueDto(name,avgRounded);
        nameValueMap.add(total);
    }

    public static double calculateAverageScore(List<NameValueDto> nameValueMap) {
        OptionalDouble average = nameValueMap.stream().mapToDouble(NameValueDto::value).average();
        return BigDecimal.valueOf(average.orElse(0)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public List<NameValueDto> getEvaluationByLastTrimestre(String projectId) {
        String projectPattern = projectId + "%";
        return evaluationEstDao.findEvaluationsByLastTrimestreTypeSafe(projectPattern);
    }

    public EvaluationStatus getEvaluationStatus(String projectId) {
        String projectPattern = projectId + "%";
        List<NameStatusDto> actionStatuses = evaluationEstDao.actionStatusByLatestTrimestre(projectPattern);
        List<NameStatusDto> detailsActionsByEst = evaluationEstDao.findDetailsActionsByEst(projectPattern);
        List<NameValueDto> reactivites = evaluationEstDao.calculateReactiviteByEst(projectPattern);
        int avgReactivite = evaluationEstDao.getGlobalAverageReactivity(projectPattern);
        int lastTrimestreAvgReactivite = evaluationEstDao.getGlobalAverageReactivityByLastTrimestre(projectPattern);

        return new EvaluationStatus(detailsActionsByEst,
                actionStatuses,
                reactivites,
                avgReactivite,
                lastTrimestreAvgReactivite);
    }
}
