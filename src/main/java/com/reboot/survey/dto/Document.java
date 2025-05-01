package com.reboot.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private String content;
    private Map<String, String> metadata;

    // 유용한 getContent 메서드 (이미 Getter에 의해 생성됨)

    // 메타데이터 접근 헬퍼 메서드 (필요한 경우)
    public String getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
}