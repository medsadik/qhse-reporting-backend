package org.example.qhsereportingbackend.utils;

import java.util.Map;

public class TablesConstant {

    public static final Map<String,String> tables = Map.of(
            "Formations","Fiche Formation",
            "Eor","EOR V5",
            "Toolbox","Fiche Toolbox",
            "Su","Fiche Simulation d'urgence",
            "Jsa","Fiche JSA",
            "Induction","Fiche d'accueil",
            "Recrutement","Fiche suivi du recrutement local");

    public static <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
