package org.example.qhsereportingbackend.IndicateursGeneraux.Dto;

import java.util.List;

public record AvancementTravaux(List<AvancementStat> data, AvancementStat latest, List<String> commentaires) {
}
