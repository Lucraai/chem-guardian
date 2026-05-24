package com.cnchem.guardian.service.impl;

import com.cnchem.guardian.dto.AdvisorAskRequest;
import com.cnchem.guardian.dto.AdvisorAskResponse;
import com.cnchem.guardian.dto.ChecklistGenerateRequest;
import com.cnchem.guardian.dto.ScenarioAnalyzeRequest;
import com.cnchem.guardian.dto.TicketPrecheckRequest;
import com.cnchem.guardian.service.AdvisorService;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class DefaultAdvisorService implements AdvisorService {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public DefaultAdvisorService(RestTemplate restTemplate,
                                 @Value("${app.ai-service-url}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    @Override
    public AdvisorAskResponse ask(AdvisorAskRequest request) {
        try {
            AdvisorAskResponse response = restTemplate.postForObject(
                    aiServiceUrl + "/ask",
                    request,
                    AdvisorAskResponse.class
            );
            if (response != null) {
                return response;
            }
        } catch (RestClientException ex) {
            // fall through to fallback response
        }
        AdvisorAskResponse fallback = new AdvisorAskResponse();
        fallback.setQuestion(request.getQuestion());
        fallback.setAnswer("AI service unavailable, fallback response from Java backend.");
        fallback.setSources(Collections.emptyList());
        fallback.setRiskLevel("UNKNOWN");
        return fallback;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> analyzeScenario(ScenarioAnalyzeRequest request) {
        try {
            Map<String, Object> response = restTemplate.postForObject(
                    aiServiceUrl + "/analyze-scenario",
                    request,
                    Map.class
            );
            if (response != null) {
                return response;
            }
        } catch (RestClientException ex) {
            // fall through to fallback response
        }
        Map<String, Object> fallback = new LinkedHashMap<String, Object>();
        fallback.put("scenario", request.getScenario());
        fallback.put("risk_level", "UNKNOWN");
        fallback.put("key_points", Collections.emptyList());
        fallback.put("recommended_actions", Collections.singletonList("AI service unavailable, please review manually."));
        return fallback;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateChecklist(ChecklistGenerateRequest request) {
        try {
            Map<String, Object> response = restTemplate.postForObject(
                    aiServiceUrl + "/generate-checklist",
                    request,
                    Map.class
            );
            if (response != null) {
                return response;
            }
        } catch (RestClientException ex) {
            // fall through to fallback response
        }
        Map<String, Object> fallback = new LinkedHashMap<String, Object>();
        fallback.put("scenario", request.getScenario());
        fallback.put("items", Collections.singletonList("AI service unavailable, please generate checklist manually."));
        fallback.put("sources", Collections.emptyList());
        return fallback;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> precheckTicket(TicketPrecheckRequest request) {
        try {
            Map<String, Object> response = restTemplate.postForObject(
                    aiServiceUrl + "/precheck-ticket",
                    request,
                    Map.class
            );
            if (response != null) {
                return response;
            }
        } catch (RestClientException ex) {
            // fall through to fallback response
        }
        Map<String, Object> fallback = new LinkedHashMap<String, Object>();
        fallback.put("ticket_type", request.getTicketType());
        fallback.put("status", "REVIEW");
        fallback.put("issues", Collections.singletonList("AI service unavailable, manual review required."));
        return fallback;
    }
}
