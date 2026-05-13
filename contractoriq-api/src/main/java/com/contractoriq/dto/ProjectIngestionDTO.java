package com.contractoriq.dto;

import com.contractoriq.models.CompletionStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ProjectIngestionDTO {

    @NotNull
    private UUID contractorId;

    @NotNull
    @Size(min = 1, max = 255)
    private String projectName;

    @NotNull
    private String city;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull
    private LocalDate plannedEndDate;

    @NotNull
    private BigDecimal budgetPlanned;

    private BigDecimal budgetActual;

    @Min(1) @Max(5)
    private Integer customerRating;

    @NotNull
    private CompletionStatus completionStatus;

    public UUID getContractorId() { return contractorId; }
    public void setContractorId(UUID contractorId) { this.contractorId = contractorId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getPlannedEndDate() { return plannedEndDate; }
    public void setPlannedEndDate(LocalDate plannedEndDate) { this.plannedEndDate = plannedEndDate; }

    public BigDecimal getBudgetPlanned() { return budgetPlanned; }
    public void setBudgetPlanned(BigDecimal budgetPlanned) { this.budgetPlanned = budgetPlanned; }

    public BigDecimal getBudgetActual() { return budgetActual; }
    public void setBudgetActual(BigDecimal budgetActual) { this.budgetActual = budgetActual; }

    public Integer getCustomerRating() { return customerRating; }
    public void setCustomerRating(Integer customerRating) { this.customerRating = customerRating; }

    public CompletionStatus getCompletionStatus() { return completionStatus; }
    public void setCompletionStatus(CompletionStatus completionStatus) { this.completionStatus = completionStatus; }
}
