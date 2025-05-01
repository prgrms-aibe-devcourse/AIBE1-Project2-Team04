package com.reboot.survey.service;

import lombok.Data;
import java.util.Map;

@Data
public class SearchResult {
    private String id;
    private float score;
    private Map<String, String> metadata;
}