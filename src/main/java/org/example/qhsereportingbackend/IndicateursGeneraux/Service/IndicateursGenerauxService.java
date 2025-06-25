package org.example.qhsereportingbackend.IndicateursGeneraux.Service;


import org.example.qhsereportingbackend.IndicateursGeneraux.Dao.IndicateursGenerauxDao;
import org.example.qhsereportingbackend.IndicateursGeneraux.Dto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class IndicateursGenerauxService {


    private final IndicateursGenerauxDao indicateursGenerauxDao;

    public IndicateursGenerauxService(IndicateursGenerauxDao indicateursGenerauxDao) {
        this.indicateursGenerauxDao = indicateursGenerauxDao;
    }

    public Effectif getEffectifInfos(String projet, LocalDate start, LocalDate end) {
        LocalDateTime starDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        EffectifStat effectifStat = indicateursGenerauxDao.getCombinedProjectStats(projet, starDateTime, endDateTime);
        List<EffectifByDate> effectifByDate = indicateursGenerauxDao.getEffectifByDate(projet);
        effectifByDate.sort(Comparator.comparing(EffectifByDate::date));
        return new Effectif(effectifStat, effectifByDate);
    }

    public AvancementTravaux getAvancementTravaux(String projet, LocalDate start, LocalDate end) {
        LocalDateTime starDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<AvancementStat> avancementStats = indicateursGenerauxDao.getAvancementStats(projet, endDateTime);
        AvancementStat latestAvancementStats = indicateursGenerauxDao.getLatestAvancementStats(projet, endDateTime);
        List<String> commentsByTravaux = indicateursGenerauxDao.findCommentsByTravaux(projet, starDateTime, endDateTime);
        return new AvancementTravaux(avancementStats, latestAvancementStats, commentsByTravaux);

    }

    public FaitMarquant getFaitsMarquant(String projet, LocalDate start, LocalDate end) {
        return indicateursGenerauxDao.findFaitsMarquants(projet, start, end);
    }

    public GlobalProjectDetails getProjetDetailsDto(String projet) {
        String projetPattern = projet + "%";
        ProjetDetailsDto latestProjetDetails = indicateursGenerauxDao.findLatestProjetDetails(projetPattern);
        double satisfactionPercentage = indicateursGenerauxDao.calculateSatisfactionPercentage(projetPattern);
        List<String> comments = indicateursGenerauxDao.getLatestProjectComments(projetPattern);
        return new GlobalProjectDetails(latestProjetDetails,satisfactionPercentage,comments);
    }

    public List<TravauxModificatif> getTravauxModificatifList(String projet) {
        String projetPattern = projet + "%";
        return indicateursGenerauxDao.getTravauxModificatifList(projetPattern);
    }
    public TravauxModificatifStat getTravauxModificatifStat(String projet) {
        String projetPattern = projet + "%";
        return indicateursGenerauxDao.getTravauxModificatifsSummary(projetPattern);
    }

}
