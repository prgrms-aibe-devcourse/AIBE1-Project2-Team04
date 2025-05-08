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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RefundService {

    @Value("${toss.apikey}")
    private String tossApiKey;

    private final PaymentRepository paymentRepository;
    private final TossRepository tossRepository;
    private final RefundRepository refundRepository;
    private final ObjectMapper objectMapper;

    // 결제 정보 조회
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));
    }

    // 환불 처리 및 환불 내역 저장
    @Transactional
    public RefundHistory processRefundWithHistory(Long paymentId, Integer amount, String reason) {
        try {
            // 1. 결제 정보 조회
            Payment payment = getPayment(paymentId);

            // 2. 환불 가능 상태 확인
            if (!"PAY_COMPLETE".equals(payment.getStatus())) {
                throw new RuntimeException("환불 가능한 상태가 아닙니다: " + payment.getStatus());
            }

            // 3. 토스 트랜잭션 정보 조회
            TossTransaction transaction = tossRepository.findByPayment(payment)
                    .orElseThrow(() -> new RuntimeException("결제 트랜잭션 정보를 찾을 수 없습니다"));

            // 4. 환불 금액 설정 (null이면 전액 환불)
            int refundAmount = (amount != null) ? amount : payment.getPrice();

            // 5. 환불 번호 생성 (UUID 사용)
            String refundNo = UUID.randomUUID().toString();

            // 6. 환불 내역 생성 및 저장
            RefundHistory refundHistory = RefundHistory.builder()
                    .payment(payment)
                    .refundNo(refundNo)
                    .refundAmount(refundAmount)
                    .refundReason(reason)
                    .requestedAt(LocalDateTime.now())
                    .status("REFUND_REQUESTED")
                    .build();

            refundRepository.save(refundHistory);

            // 7. 토스 환불 API 호출
            Map<String, Object> apiResponse = requestTossRefund(transaction.getPayToken(), refundNo, refundAmount, reason);

            // 8. 환불 결과 처리
            boolean isSuccess = Integer.valueOf(0).equals(apiResponse.get("code"));

            if (isSuccess) {
                // 9. 환불 성공 시 상태 업데이트
                if (refundAmount == payment.getPrice()) {
                    payment.setStatus("REFUND_COMPLETE"); // 전액 환불
                } else {
                    payment.setStatus("PARTIAL_REFUND"); // 부분 환불
                }
                paymentRepository.save(payment);

                // 10. 트랜잭션 정보 업데이트
                transaction.setTossStatus(payment.getStatus());
                tossRepository.save(transaction);

                // 11. 환불 내역 업데이트
                refundHistory.setStatus("REFUND_COMPLETED");
                refundHistory.setCompletedAt(LocalDateTime.now());
                refundHistory.setResponseCode("0");
                refundHistory.setResponseMessage("환불 성공");
            } else {
                // 12. 환불 실패 시
                refundHistory.setStatus("REFUND_FAILED");
                refundHistory.setResponseCode(apiResponse.get("code").toString());
                refundHistory.setResponseMessage(apiResponse.get("message").toString());
            }

            return refundRepository.save(refundHistory);
        } catch (Exception e) {
            System.err.println("환불 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("환불 처리 중 오류: " + e.getMessage(), e);
        }
    }

    // 토스 환불 API 호출
    private Map<String, Object> requestTossRefund(String payToken, String refundNo, int amount, String reason) throws Exception {
        try {
            System.out.println("Using API Key: " + tossApiKey.substring(0, 5) + "...");
            // 1. 환불 요청 URL 설정
            URL url = new URL("https://api.tosspayments.com/v1/payments/" + payToken + "/cancel");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((tossApiKey + ":").getBytes(StandardCharsets.UTF_8)));
            connection.setDoOutput(true);

            // 2. 요청 본문 작성
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("reason", reason != null ? reason : "고객 요청");
            requestMap.put("amount", amount);


            // 3. 요청 전송
            try (OutputStream os = connection.getOutputStream()) {
                os.write(objectMapper.writeValueAsBytes(requestMap));
                os.flush();
            }

            // 4. 응답 읽기
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

            // 5. 응답 파싱
            return objectMapper.readValue(
                    responseBody.toString(), new TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            System.err.println("토스 환불 API 호출 중 오류: " + e.getMessage());
            throw e;
        }
    }

    // 환불 상태 확인
    public String checkRefundStatus(Long paymentId) throws Exception {
        Payment payment = getPayment(paymentId);
        TossTransaction transaction = tossRepository.findByPayment(payment)
                .orElseThrow(() -> new RuntimeException("결제 트랜잭션 정보를 찾을 수 없습니다"));

        // 토스 상태 확인 API 호출
        return checkTossPaymentStatus(transaction.getPayToken());
    }

    // 토스 결제 상태 확인 API 호출
    private String checkTossPaymentStatus(String payToken) throws Exception {
        try {
            // 1. 상태 확인 요청 URL 설정
            URL url = new URL("https://api.tosspayments.com/v1/payments/" + payToken);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + tossApiKey);

            // 2. 응답 읽기
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBody.append(line);
                }
            }

            // 3. 응답 파싱
            Map<String, Object> responseMap = objectMapper.readValue(
                    responseBody.toString(), new TypeReference<Map<String, Object>>() {});

            // 4. 결제 상태 반환
            return (String) responseMap.get("status");
        } catch (Exception e) {
            System.err.println("토스 상태 확인 API 호출 중 오류: " + e.getMessage());
            throw e;
        }
    }

    // 결제별 환불 내역 조회
    public List<RefundHistory> getRefundHistoriesByPayment(Long paymentId) {
        Payment payment = getPayment(paymentId);
        return refundRepository.findByPayment(payment);
    }

    // 환불 번호로 환불 내역 조회
    public RefundHistory getRefundHistoryByRefundNo(String refundNo) {
        return refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new RuntimeException("환불 내역을 찾을 수 없습니다: " + refundNo));
    }
}