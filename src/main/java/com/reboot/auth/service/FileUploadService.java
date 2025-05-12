package com.reboot.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {

    @Value("${spring.supabase.url}")
    private String supabaseUrl;

    @Value("${spring.supabase.access-key}")
    private String supabaseAccessKey;

    @Value("${spring.supabase.img-bucket-name}")
    private String imgBucketName;  // 기존 프로필 이미지용 버킷

    // 강의 이미지 전용 버킷
    @Value("${spring.supabase.lecture-img-bucket-name:instructor-lecture-images}")
    private String lectureBucketName;

    // 기존 프로필 이미지 업로드
    public String uploadImageToSupabase(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        // TODO: 실제 Supabase API 구현 필요
        Path uploadPath = Paths.get("uploads", "profiles");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        return supabaseUrl + "/storage/v1/object/public/" + imgBucketName + "/" + filename;
    }

    // 강의 이미지 업로드 (전용 버킷 사용)
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

        // TODO: 실제 Supabase API 구현 필요
        Path uploadPath = Paths.get("uploads", lectureBucketName, folderPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        // 강의 이미지 버킷 URL
        return String.format("%s/storage/v1/object/public/%s/%s/%s",
                supabaseUrl, lectureBucketName, folderPath, filename);
    }

    // 이미지 삭제 메서드
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // 어느 버킷인지 확인
            String profileBucketUrl = String.format("%s/storage/v1/object/public/%s/",
                    supabaseUrl, imgBucketName);
            String lectureBucketUrl = String.format("%s/storage/v1/object/public/%s/",
                    supabaseUrl, lectureBucketName);

            String path = null;
            String bucketName = null;

            if (imageUrl.startsWith(profileBucketUrl)) {
                path = imageUrl.substring(profileBucketUrl.length());
                bucketName = imgBucketName;
            } else if (imageUrl.startsWith(lectureBucketUrl)) {
                path = imageUrl.substring(lectureBucketUrl.length());
                bucketName = lectureBucketName;
            }

            if (path != null) {
                // TODO: 실제 Supabase API를 사용한 삭제 구현 필요
                log.info("이미지 삭제 - 버킷: {}, 경로: {}", bucketName, path);

                // 임시 로컬 삭제
                Path filePath = Paths.get("uploads", bucketName, path);
                Files.deleteIfExists(filePath);
            }

        } catch (Exception e) {
            log.error("이미지 삭제 실패: ", e);
        }
    }

    // 이미지 파일 검증
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}