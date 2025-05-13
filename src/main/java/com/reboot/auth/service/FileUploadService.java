package com.reboot.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {

    @Value("${spring.supabase.url}")
    private String supabaseUrl;

    @Value("${spring.supabase.access-key}")
    private String supabaseAccessKey;

    @Value("${spring.supabase.img-bucket-name}")
    private String imgBucketName;

    @Value("${spring.supabase.lecture-img-bucket-name:instructor-lecture-images}")
    private String lectureBucketName;

    private final RestTemplate restTemplate = new RestTemplate();

    // 강의 이미지 업로드 (실제 Supabase API 사용)
    public String uploadLectureImage(MultipartFile file, Long instructorId) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // 파일 검증
        if (!isImageFile(file)) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 파일 크기 체크 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        // 강사별 폴더 구조
        String folderPath = String.format("instructor-%d", instructorId);
        String fullPath = folderPath + "/" + filename;

        try {
            // Supabase Storage API URL
            String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                    supabaseUrl, lectureBucketName, fullPath);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            headers.setBearerAuth(supabaseAccessKey);

            // 요청 생성
            HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);

            // 파일 업로드
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // 업로드 성공 시 공개 URL 반환
                return String.format("%s/storage/v1/object/public/%s/%s",
                        supabaseUrl, lectureBucketName, fullPath);
            } else {
                throw new IOException("Supabase 업로드 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Supabase 업로드 중 오류 발생: ", e);
            throw new IOException("파일 업로드 실패", e);
        }
    }

    // 이미지 삭제 메서드 (실제 Supabase API 사용)
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String lectureBucketUrl = String.format("%s/storage/v1/object/public/%s/",
                    supabaseUrl, lectureBucketName);

            if (imageUrl.startsWith(lectureBucketUrl)) {
                String path = imageUrl.substring(lectureBucketUrl.length());

                // Supabase Storage API URL
                String deleteUrl = String.format("%s/storage/v1/object/%s/%s",
                        supabaseUrl, lectureBucketName, path);

                // HTTP 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(supabaseAccessKey);

                // 요청 생성
                HttpEntity<Void> request = new HttpEntity<>(headers);

                // 파일 삭제
                ResponseEntity<String> response = restTemplate.exchange(
                        deleteUrl,
                        HttpMethod.DELETE,
                        request,
                        String.class
                );

                if (response.getStatusCode() == HttpStatus.OK) {
                    log.info("이미지 삭제 성공: {}", path);
                } else {
                    log.error("이미지 삭제 실패: {}", response.getStatusCode());
                }
            }

        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생: ", e);
        }
    }

    // 이미지 파일 검증
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    // 기존 프로필 이미지 업로드
    public String uploadImageToSupabase(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        try {
            // Supabase Storage API URL
            String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                    supabaseUrl, imgBucketName, filename);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            headers.setBearerAuth(supabaseAccessKey);

            // 요청 생성
            HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);

            // 파일 업로드
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return String.format("%s/storage/v1/object/public/%s/%s",
                        supabaseUrl, imgBucketName, filename);
            } else {
                throw new IOException("Supabase 업로드 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Supabase 업로드 중 오류 발생: ", e);
            throw new IOException("파일 업로드 실패", e);
        }
    }
}