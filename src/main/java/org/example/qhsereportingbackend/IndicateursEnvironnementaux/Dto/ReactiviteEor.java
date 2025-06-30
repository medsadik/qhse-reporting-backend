package org.example.qhsereportingbackend.IndicateursEnvironnementaux.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record ReactiviteEor(List<NameValueDto> reactiviteByCategories, List<NameValueDto> reactiviteByEst,
                            List<NameValueDto> reactiviteByHierarchieDeControle) {
}
