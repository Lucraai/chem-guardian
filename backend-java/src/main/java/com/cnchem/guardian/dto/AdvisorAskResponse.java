package com.cnchem.guardian.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AdvisorAskResponse {

    private String question;
    private String answer;
    private List<AdvisorSourceRef> sources;
    @JsonProperty("risk_level")
    private String riskLevel;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<AdvisorSourceRef> getSources() {
        return sources;
    }

    public void setSources(List<AdvisorSourceRef> sources) {
        this.sources = sources;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
