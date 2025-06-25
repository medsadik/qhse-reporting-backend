package org.example.qhsereportingbackend.EvaluationEST.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class Status {
    private String status;
    private int totalActions;

    public Status(String status, int totalActions) {
        this.status = status;
        this.totalActions = totalActions;
    }
    public String getStatus() {
        return status;
    }

    public int getTotalActions() {
        return totalActions;
    }

}
