package com.reboot.auth.service;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadService {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket.name}")
    private String bucketName;

    public String uploadImageToSupabase(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.warn("업로드할 파일이 없습니다.");
            return null;
        }

        logger.info("프로필 이미지 업로드 시작: {}, 크기: {}", file.getOriginalFilename(), file.getSize());

        // 파일 이름 생성 (충돌 방지를 위해 UUID 사용)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // 파일 경로 설정 (profiles 폴더 내)
        String filePath = "profiles/" + fileName;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Supabase Storage API 엔드포인트
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;
            logger.info("Supabase 업로드 URL: {}", uploadUrl);

            // HTTP POST 요청 설정
            HttpPost httpPost = new HttpPost(uploadUrl);
            httpPost.setHeader("Authorization", "Bearer " + supabaseKey);
            httpPost.setHeader("Content-Type", "multipart/form-data");

            // 파일을 멀티파트 폼으로 포장
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody(
                    "file",
                    file.getInputStream(),
                    ContentType.parse(file.getContentType()),
                    fileName
            );

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            // 요청 실행
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();

                if (statusCode >= 200 && statusCode < 300) {
                    // 업로드 성공, 공개 URL 생성
                    String imageUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;
                    logger.info("프로필 이미지 업로드 완료: {}", imageUrl);
                    return imageUrl;
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    logger.error("Supabase 업로드 실패: {} {}", statusCode, responseBody);
                    throw new IOException("Supabase 업로드 실패: " + statusCode + " " + responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("이미지 업로드 중 오류 발생", e);
            throw new IOException("이미지 업로드 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // 테스트용 Supabase 연결 정보 반환
    public String getSupabaseInfo() {
        return "URL: " + supabaseUrl + ", Bucket: " + bucketName;
    }
}
