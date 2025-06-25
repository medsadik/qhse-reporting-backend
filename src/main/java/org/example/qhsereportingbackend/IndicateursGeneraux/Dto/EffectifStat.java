package org.example.qhsereportingbackend.IndicateursGeneraux.Dto;

import java.time.LocalDate;

public record EffectifStat(LocalDate lastDate,
                           int jourTravaille,
                           int heuresProjet,
                           int heuresProjetParSemaine,
                           double moyyenneHebdo,
                           double moyenneHeureParJour,
                           int effectifAjour) {
    public static EffectifStat combine(EffectifStat s1, EffectifStat s2) {
        if (s1 == null && s2 == null) {
            return null;
        } else if (s1 == null) {
            return s2;
        } else if (s2 == null) {
            return s1;
        }
        LocalDate lastDate = s1.lastDate().isAfter(s2.lastDate()) ? s1.lastDate() : s2.lastDate();
        int jours = s1.jourTravaille() + s2.jourTravaille();
        int heures = s1.heuresProjet() + s2.heuresProjet();
        int heuresSemaine = s1.heuresProjetParSemaine() + s2.heuresProjetParSemaine();
        long moyenneJour = Math.round((s1.moyenneHeureParJour() + s2.moyenneHeureParJour()) / 2.0);
        int maxEffectif = Math.max(s1.effectifAjour(), s2.effectifAjour());
        long moyenneHebdo = Math.round((s1.moyyenneHebdo() + s2.moyyenneHebdo()) / 2.0);

        return new EffectifStat(lastDate, jours, heures, heuresSemaine, moyenneJour,moyenneHebdo,maxEffectif);
    }

}
