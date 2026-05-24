package com.cnchem.guardian.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.constraints.NotBlank;

public class TicketPrecheckRequest {

    @NotBlank
    private String ticketType;

    private Map<String, Object> content = new LinkedHashMap<String, Object>();

    private Long tenantId = 1L;

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}

