package org.example.qhsereportingbackend.IndicateursSST.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameStatusDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.List;

public record SorDTO(List<NameValueDto> tableStats, List<NameValueDto> globalStatut,
                     List<NameStatusDto> globalStatuByCategorie,
                     List<NameStatusDto> globalStatuByEst,
                     List<NameStatusDto> hebdoStatuByCategorie,
                     List<NameStatusDto> hebdoStatuByEst, List<NameValueDto> reactiviteByCategorie,
                     List<NameValueDto> reactiviteByEst, List<NameValueDto> reactiviteByHierachieControle)  {
}
