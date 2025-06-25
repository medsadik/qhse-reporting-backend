package org.example.qhsereportingbackend.IndicateursSST.Dto;

import org.example.qhsereportingbackend.GlobalDto.NameObjectDto;
import org.example.qhsereportingbackend.GlobalDto.NameValueDto;

import java.util.ArrayList;
import java.util.List;

public record FormationOrToolBoxDto(List<NameObjectDto> globalStats,List<NameObjectDto> hebdoStats,String lastDate, List<NameValueDto> totalThematiques,
                                    List<NameValueDto> hebdoThematiques) {


}
