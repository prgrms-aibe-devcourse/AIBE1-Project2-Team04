package com.reboot.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reboot.payment.entity.Payment;
import com.reboot.payment.entity.RefundHistory;
import com.reboot.payment.entity.TossTransaction;
import com.reboot.payment.repository.PaymentRepository;
import com.reboot.payment.repository.RefundRepository;
import com.reboot.payment.repository.TossRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor // 생성자 주입을 위한 Lombok 어노테이션
public class RefundService {

    // application.properties 또는 application.yml에 정의된 Toss API 키 주입
    @Value("${toss.apikey}")
    private String tossApiKey;

    // 필요한 Repository와 ObjectMapper 주입
    private final PaymentRepository paymentRepository;
    private final TossRepository tossRepository;
    private final RefundRepository refundRepository;
    private final ObjectMapper objectMapper;

    /**
     * 결제 ID를 기반으로 결제 정보를 조회하는 메서드
     */
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다"));
    }

    // 환불 처리와 동시에 환불 내역 저장
    @Transactional // DB 트랜잭션 처리 보장
    public RefundHistory processRefundWithHistory(Long paymentId, Integer amount, String reason) throws Exception {
        // 결제 정보 조회
        Payment payment = getPayment(paymentId);

        // 해당 결제에 대한 Toss 거래 정보 조회
        TossTransaction transaction = tossRepository.findByPayment(payment)
                .orElseThrow(() -> new RuntimeException("결제 트랜잭션 정보를 찾을 수 없습니다"));
        String payToken = transaction.getPayToken(); // Toss 결제 토큰 추출

        // 고유 환불 번호 생성 (UUID 사용)
        String refundNo = UUID.randomUUID().toString();

        // Toss 환불 API 호출하여 환불 요청 처리
        Map<String, Object> response = requestTossPayRefund(payToken, refundNo, amount, reason);

        // 응답 코드가 0이면 성공으로 간주
        boolean isSuccess = Integer.valueOf(0).equals(response.get("code"));

        // 환불 내역 객체 생성
        RefundHistory refundHistory = RefundHistory.builder()
                .payment(payment)
                .refundNo(refundNo)
                .refundAmount(amount != null ? amount : payment.getPrice()) // null이면 전체 금액 환불
                .refundReason(reason)
                .requestedAt(LocalDateTime.now())
                .status(isSuccess ? "환불 완료" : "환불 실패") // 상태 저장
                .responseCode(String.valueOf(response.get("code")))
                .responseMessage((String) response.getOrDefault("msg", "")) // 메시지가 없을 경우 빈 문자열
                .completedAt(isSuccess ? LocalDateTime.now() : null) // 성공 시 환불 완료 시간 저장
                .build();

        // DB에 환불 내역 저장 후 반환
        return refundRepository.save(refundHistory);

    }

    /**
     * Toss API에 POST 요청을 보내 환불을 처리하는 메서드
     */
    private Map<String, Object> requestTossPayRefund(String payToken, String refundNo, Integer amount, String reason) throws Exception {
        URL url = new URL("https://pay.toss.im/api/v2/refunds");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // POST 방식 설정 및 JSON 형식 명시
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true); // body 데이터 전송 허용

        // 요청에 사용할 데이터 구성
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("apiKey", tossApiKey);
        requestMap.put("payToken", payToken);
        requestMap.put("refundNo", refundNo);
        if (amount != null) requestMap.put("amount", amount);
        if (reason != null && !reason.isBlank()) requestMap.put("reason", reason);

        // JSON 데이터를 OutputStream으로 전송
        try (OutputStream os = connection.getOutputStream()) {
            os.write(objectMapper.writeValueAsBytes(requestMap));
            os.flush();
        }

        // 응답 코드에 따라 InputStream 또는 ErrorStream 읽기
        int responseCode = connection.getResponseCode();
        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBody.append(line);
            }
        }

        // 응답 JSON 문자열을 Map으로 변환하여 반환
        return objectMapper.readValue(
                responseBody.toString(), new TypeReference<Map<String, Object>>() {});
    }


    /**
     * 환불 번호(refundNo)를 통해 환불 이력 조회
     */
    public RefundHistory getRefundHistoryByRefundNo(String refundNo) {
        return refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new RuntimeException("환불 내역을 찾을 수 없습니다"));
    }
}