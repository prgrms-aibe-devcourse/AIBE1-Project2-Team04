package com.reboot.survey.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public interface VectorDbClient {
    void upsert(String id, float[] embedding, Map<String, String> metadata);
    List<SearchResult> search(float[] queryVector, Map<String, Object> filter, int limit, float threshold);
}