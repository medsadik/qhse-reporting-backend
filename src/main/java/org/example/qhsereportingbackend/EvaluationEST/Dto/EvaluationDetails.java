package org.example.qhsereportingbackend.EvaluationEST.Dto;

import java.util.List;

public record EvaluationDetails(List<org.example.qhsereportingbackend.GlobalDto.NameValueDto> actionDetail,
                                List<org.example.qhsereportingbackend.GlobalDto.NameValueDto> reunionDetail,
                                int totalActions,
                                int totalPvs) {
}
