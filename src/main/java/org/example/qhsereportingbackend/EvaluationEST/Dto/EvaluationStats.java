package org.example.qhsereportingbackend.EvaluationEST.Dto;

import java.util.List;

public record EvaluationStats(EvaluationDetails evaluationDetails , List<org.example.qhsereportingbackend.GlobalDto.NameValueDto> trimestre) {
}
