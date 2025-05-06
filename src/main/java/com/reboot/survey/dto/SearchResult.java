package com.reboot.survey.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SearchResult {
    private Long id;
    private float score;
    private Map<String, String> metadata;
}