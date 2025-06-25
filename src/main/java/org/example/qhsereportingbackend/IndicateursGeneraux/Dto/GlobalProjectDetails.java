package org.example.qhsereportingbackend.IndicateursGeneraux.Dto;

import java.util.List;

public record GlobalProjectDetails(ProjetDetailsDto detailProjet, double satisfaction,  List<String> commentaires) {
}
