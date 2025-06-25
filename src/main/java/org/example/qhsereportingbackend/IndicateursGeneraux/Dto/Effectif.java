package org.example.qhsereportingbackend.IndicateursGeneraux.Dto;

import java.util.List;

public record Effectif(EffectifStat effectifStat,
                    List<EffectifByDate> effectifByDate) {
}
