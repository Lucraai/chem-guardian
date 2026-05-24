package com.cnchem.guardian.dto;

import javax.validation.constraints.NotBlank;

public class AdvisorAskRequest {

    @NotBlank
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}

