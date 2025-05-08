package com.reboot.auth.service;

import com.reboot.auth.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${spring.supabase.url}")
    private String supabaseUrl;

    @Value("${spring.supabase.access-key}")
    private String supabaseAccessKey;

    @Value("${spring.supabase.img-bucket-name}")
    private String imgBucketName;

    // Supabase 스토리지에 이미지 업로드 (실제 구현은 Supabase API에 맞게 조정 필요)
    public String uploadImageToSupabase(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String filename = UUID.randomUUID().toString() + extension;

        Path uploadPath = Paths.get("uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        return supabaseUrl + "/storage/v1/object/public/" + imgBucketName + "/" + filename;
    }
}