package com.reboot.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket.name}")
    private String bucketName;

    // 경계 문자열 생성
    private static String generateBoundary() {
        return "---------------------------" + UUID.randomUUID().toString().replace("-", "");
    }

    // Supabase Storage에 이미지 업로드
    public String uploadImageToSupabase(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일 이름 생성 (충돌 방지를 위해 UUID 사용)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // 파일 경로 설정 (profiles 폴더 내)
        String filePath = "profiles/" + fileName;

        // Supabase Storage API 엔드포인트
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;

        // 경계 문자열 생성
        String boundary = generateBoundary();

        // 연결 설정
        URL url = new URL(uploadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + supabaseKey);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

            // 파일 데이터 전송을 위한 경계 추가
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
            if (file.getContentType() != null) {
                writer.append("Content-Type: ").append(file.getContentType()).append("\r\n");
            } else {
                writer.append("Content-Type: application/octet-stream\r\n");
            }
            writer.append("\r\n");
            writer.flush();

            // 파일 바이너리 데이터 전송
            try (InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            // 종료 경계 추가
            writer.append("\r\n");
            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.flush();
        }

        // 응답 코드 확인
        int responseCode = connection.getResponseCode();

        if (responseCode >= 200 && responseCode < 300) {
            // 업로드 성공, 공개 URL 생성
            String imageUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;
            System.out.println("프로필 이미지 업로드 완료: " + imageUrl);
            return imageUrl;
        } else {
            // 오류 응답 읽기
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
            }
            System.err.println("Supabase 업로드 실패: " + responseCode + " " + errorResponse);
            throw new IOException("Supabase 업로드 실패: " + responseCode + " " + errorResponse);
        }
    }

    public String getSupabaseInfo() {
        return "URL: " + supabaseUrl + ", Bucket: " + bucketName;
    }
}
