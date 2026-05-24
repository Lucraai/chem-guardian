package com.cnchem.guardian.dto;

import javax.validation.constraints.NotBlank;

public class ScenarioAnalyzeRequest {

    @NotBlank
    private String scenario;

    private Long tenantId = 1L;

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}

