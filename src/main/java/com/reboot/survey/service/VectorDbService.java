package com.reboot.survey.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class VectorDbService {

    private final VectorDbClient vectorDbClient;
    private final EmbeddingService embeddingService;

    @Autowired
    public VectorDbService(VectorDbClient vectorDbClient, EmbeddingService embeddingService) {
        this.vectorDbClient = vectorDbClient;
        this.embeddingService = embeddingService;
    }

    public List<SearchResult> hybridSearch(String searchQuery, Map<String, Object> filter, int limit) {
        // 검색 쿼리를 임베딩 벡터로 변환
        float[] queryEmbedding = embeddingService.generateEmbedding(searchQuery);

        // 벡터 DB 검색 실행
        return vectorDbClient.search(queryEmbedding, filter, limit, 0.7f); // 임계값 0.7 설정
    }
}