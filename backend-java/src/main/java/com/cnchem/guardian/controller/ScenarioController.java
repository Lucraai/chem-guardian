package com.cnchem.guardian.controller;

import com.cnchem.guardian.dto.ChecklistGenerateRequest;
import com.cnchem.guardian.dto.ScenarioAnalyzeRequest;
import com.cnchem.guardian.dto.TicketPrecheckRequest;
import com.cnchem.guardian.service.AdvisorService;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class ScenarioController {

    private final AdvisorService advisorService;

    public ScenarioController(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @PostMapping(value = "/api/advisor/analyze-scenario", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> analyzeScenario(@Valid @RequestBody ScenarioAnalyzeRequest request) {
        return advisorService.analyzeScenario(request);
    }

    @PostMapping(value = "/api/advisor/generate-checklist", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> generateChecklist(@Valid @RequestBody ChecklistGenerateRequest request) {
        return advisorService.generateChecklist(request);
    }

    @PostMapping(value = "/api/advisor/precheck-ticket", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> precheckTicket(@Valid @RequestBody TicketPrecheckRequest request) {
        return advisorService.precheckTicket(request);
    }
}

