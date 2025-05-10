package com.reboot.survey.dto;

import lombok.Data;

@Data
public class LlmRequest {
    private String systemPrompt;
    private String userPrompt;
    private String responseFormat;
    private float temperature;
    private int maxTokens;
}
