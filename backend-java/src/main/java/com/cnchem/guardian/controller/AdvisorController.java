package com.cnchem.guardian.controller;

import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.cnchem.guardian.dto.AdvisorAskRequest;
import com.cnchem.guardian.dto.AdvisorAskResponse;
import com.cnchem.guardian.service.AdvisorService;

@RestController
@Validated
public class AdvisorController {

    private final AdvisorService advisorService;

    public AdvisorController(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @PostMapping(value = "/api/advisor/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AdvisorAskResponse ask(@Valid @RequestBody AdvisorAskRequest request) {
        return advisorService.ask(request);
    }
}
