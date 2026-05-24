package com.cnchem.guardian.service;

import com.cnchem.guardian.dto.AdvisorAskRequest;
import com.cnchem.guardian.dto.AdvisorAskResponse;
import com.cnchem.guardian.dto.ChecklistGenerateRequest;
import com.cnchem.guardian.dto.ScenarioAnalyzeRequest;
import com.cnchem.guardian.dto.TicketPrecheckRequest;
import java.util.Map;

public interface AdvisorService {
    AdvisorAskResponse ask(AdvisorAskRequest request);
    Map<String, Object> analyzeScenario(ScenarioAnalyzeRequest request);
    Map<String, Object> generateChecklist(ChecklistGenerateRequest request);
    Map<String, Object> precheckTicket(TicketPrecheckRequest request);
}
