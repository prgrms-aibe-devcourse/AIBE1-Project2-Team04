package com.reboot.lecture.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class LectureFileService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${spring.supabase.access-key}")
    private String supabaseKey;

    @Value("${supabase.video-bucket-name:videos}")
    private String videoBucketName;

    @Value("${supabase.img-bucket-name:images}")
    private String imgBucketName;

    @Value("${supabase.pdf-bucket-name:docs}")
    private String docBucketName;

    // 경계 문자열 생성
    private static String generateBoundary() {
        return "---------------------------" + UUID.randomUUID().toString().replace("-", "");
    }

    // 강의 썸네일 업로드
    public String uploadThumbnail(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String filePath = folder + "/" + generateUniqueFileName(file.getOriginalFilename());
        return uploadFileToSupabase(file, imgBucketName, filePath);
    }

    // 강의 비디오 업로드
    public String uploadVideo(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String filePath = folder + "/" + generateUniqueFileName(file.getOriginalFilename());
        return uploadFileToSupabase(file, videoBucketName, filePath);
    }

    // 강의 자료 업로드
    public String uploadMaterial(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String filePath = folder + "/" + generateUniqueFileName(file.getOriginalFilename());
        return uploadFileToSupabase(file, docBucketName, filePath);
    }

    // 파일 업로드 공통 로직
    private String uploadFileToSupabase(MultipartFile file, String bucketName, String filePath) throws IOException {
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

        // 파일 이름 추출
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

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
            String fileUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;
            System.out.println("강의 파일 업로드 완료: " + fileUrl);
            return fileUrl;
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

    // 유니크한 파일명 생성
    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 파일이 Supabase 스토리지에 존재하는지 확인
     * @param fileUrl 확인할 파일의 전체 URL
     * @return 파일 존재 여부
     */
    public boolean checkFileExists(String fileUrl) {
        try {
            // 1. fileUrl에서 필요한 정보 추출 (버킷과 경로)
            String bucketName = "";
            String filePath = "";

            // URL에서 버킷과 경로 추출
            // 예: https://supabase-url/storage/v1/object/public/bucket-name/path/to/file.jpg
            if (fileUrl.contains("/storage/v1/object/public/")) {
                String pathAfterPublic = fileUrl.split("/storage/v1/object/public/")[1];
                int firstSlashIndex = pathAfterPublic.indexOf("/");

                if (firstSlashIndex > 0) {
                    bucketName = pathAfterPublic.substring(0, firstSlashIndex);
                    filePath = pathAfterPublic.substring(firstSlashIndex + 1);
                } else {
                    // 경로가 없는 경우
                    bucketName = pathAfterPublic;
                    filePath = "";
                }
            } else {
                // URL 형식이 예상과 다름
                System.err.println("잘못된 Supabase URL 형식: " + fileUrl);
                return false;
            }

            // 2. Supabase Storage API를 사용하여 파일 존재 확인
            // HEAD 요청을 사용하여 파일 존재 여부만 확인
            String apiUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("Authorization", "Bearer " + supabaseKey);

            int responseCode = connection.getResponseCode();

            // 200 OK는 파일이 존재함
            // 404 Not Found는 파일이 존재하지 않음
            return (responseCode == HttpURLConnection.HTTP_OK);

        } catch (Exception e) {
            System.err.println("파일 존재 확인 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 파일 정보 조회 (메타데이터)
     * @param fileUrl 조회할 파일의 전체 URL
     * @return 파일 메타데이터 정보 문자열 (JSON)
     */
    public String getFileInfo(String fileUrl) {
        try {
            // fileUrl에서 필요한 정보 추출 (버킷과 경로)
            String bucketName = "";
            String filePath = "";

            // URL에서 버킷과 경로 추출
            if (fileUrl.contains("/storage/v1/object/public/")) {
                String pathAfterPublic = fileUrl.split("/storage/v1/object/public/")[1];
                int firstSlashIndex = pathAfterPublic.indexOf("/");

                if (firstSlashIndex > 0) {
                    bucketName = pathAfterPublic.substring(0, firstSlashIndex);
                    filePath = pathAfterPublic.substring(firstSlashIndex + 1);
                } else {
                    bucketName = pathAfterPublic;
                    filePath = "";
                }
            } else {
                return "잘못된 URL 형식";
            }

            // Supabase Storage API를 사용하여 파일 정보 조회
            String apiUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + supabaseKey);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 응답 읽기
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                return response.toString();
            } else {
                return "파일 정보 조회 실패: HTTP " + responseCode;
            }

        } catch (Exception e) {
            return "오류: " + e.getMessage();
        }
    }
}