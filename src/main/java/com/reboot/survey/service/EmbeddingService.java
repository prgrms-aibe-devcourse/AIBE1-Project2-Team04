package com.reboot.survey.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public interface EmbeddingService {
    float[] generateEmbedding(String text);
}