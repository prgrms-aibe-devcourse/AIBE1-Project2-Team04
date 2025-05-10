package com.reboot.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reboot.payment.dto.TossRequestDto;
import com.reboot.payment.dto.TossResponseDto;
import com.reboot.payment.entity.Payment;
import com.reboot.payment.entity.TossTransaction;
import com.reboot.payment.repository.PaymentRepository;
import com.reboot.payment.repository.TossRepository;
import com.reboot.reservation.entity.Reservation;
import com.reboot.reservation.repository.ReservationRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${toss.apikey}")
    private String tossApiKey;

    @Value("${app.server-url}")
    private String serverUrl;

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TossRepository tossrepository;
    private final ObjectMapper objectMapper; // JSON 파싱을 위한 객체

    @Transactional
    public String requestTossPayment(Long reservationId) {
        // 1. 예약 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다: " + reservationId));

        // 2. Payment 엔티티 생성 및 저장
        Payment payment = Payment.builder()
                .reservation(reservation)
                .price(reservation.getLecture().getInfo().getPrice()) // 예약 정보에 따른 가격 계산 메서드 필요
                .paymentAt(LocalDateTime.now())
                .status("결제 대기") // 초기 상태
                .build();

        payment = paymentRepository.save(payment);

        // 3. 토스 결제 요청 DTO 생성
        String orderNo = payment.getPaymentId() + "-" + System.currentTimeMillis();
        TossRequestDto requestDto = TossRequestDto.builder()
                .orderNo(orderNo)
                .amount(payment.getPrice())
                .amountTaxFree(0)
                .productDesc(getProductDescription(reservation)) // 상품 설명 생성 메서드 필요
                .apiKey(tossApiKey)
                .autoExecute(true)
                .resultCallback(serverUrl + "/payments/callback")
                .retUrl(serverUrl + "/payments/success?orderId=" + orderNo + "&paymentId=" + payment.getPaymentId())
                .retCancelUrl(serverUrl + "/payments/cancel?paymentId=" + payment.getPaymentId())
                .build();

        try {
            // 4. 토스 API 호출
            URL url = new URL("https://pay.toss.im/api/v2/payments");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 요청 본문 작성
            try (OutputStream os = connection.getOutputStream()) {
                os.write(objectMapper.writeValueAsBytes(requestDto));
                os.flush();
            }

            // 응답 읽기
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBody.append(line);
                }
            }

            // 5. 응답 파싱
            TossResponseDto responseDto = objectMapper.readValue(
                    responseBody.toString(), TossResponseDto.class);

            // 6. TossPaymentTransaction 엔티티 생성 및 저장
            TossTransaction transaction = TossTransaction.builder()
                    .payment(payment)
                    .orderNo(orderNo)
                    .amount(requestDto.getAmount())
                    .amountTaxFree(requestDto.getAmountTaxFree())
                    .productDesc(requestDto.getProductDesc())
                    .apiKey(requestDto.getApiKey())
                    .autoExecute(requestDto.getAutoExecute())
                    .resultCallback(requestDto.getResultCallback())
                    .retUrl(requestDto.getRetUrl())
                    .retCancelUrl(requestDto.getRetCancelUrl())
                    .payToken(responseDto.getPayToken())
                    .checkoutPage(responseDto.getCheckoutPage())
                    .responseCode(String.valueOf(responseDto.getCode()))
                    .requestedAt(LocalDateTime.now())
                    .tossStatus("PAY_PROGRESS")
                    .build();

            // 트랜잭션 저장을 위한 별도의 Repository가 필요합니다
            tossrepository.save(transaction);

            return responseDto.getCheckoutPage();

        } catch (Exception e) {
            // 예외 발생 시 Payment 상태 업데이트
            payment.setStatus("FAIL");
            paymentRepository.save(payment);
            throw new RuntimeException("토스 결제 요청 실패: " + e.getMessage(), e);
        }
    }

    // 상품 설명 생성 메서드
    private String getProductDescription(Reservation reservation) {
        // 예: "[강의명] 강사명 - 강의일정"
        return "[" + reservation.getLecture().getInfo().getTitle() + "] " +
                reservation.getInstructor().getMember().getName() + " - " +
                reservation.getScheduleDate();
    }

    // 결제 성공 처리 메서드
    @Transactional
    public void processPaymentSuccess( String orderNo, Integer amount) {
        System.out.println("결제 성공 처리 시작: orderNo=" + orderNo + ", amount=" + amount);

        try {
            // 1. orderNo에서 paymentId 추출
            String[] parts = orderNo.split("-");
            System.out.println("orderNo 분할 결과: " + String.join(", ", parts));

            Long paymentId = Long.parseLong(parts[0]);
            System.out.println("추출된 paymentId: " + paymentId);

            // 2. Payment 조회
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));
            System.out.println("Payment 조회 성공: " + payment.getPaymentId());

            // 3. Payment 상태 업데이트
            payment.setStatus("결제완료");
            payment.setMethod("TOSS");
            paymentRepository.save(payment);
            System.out.println("Payment 상태 업데이트 완료");

        } catch (Exception e) {
            System.err.println("결제 성공 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e; // 예외 다시 던지기
        }
    }

    //TossTransaction 저장
    public void saveTossTransaction(String orderNo, String status, String payMethod, String cardCompany, String bankCode) {
        Optional<TossTransaction> optional = tossrepository.findByOrderNo(orderNo);

        if (optional.isPresent()) {
            // 이미 결제 내역이 있다면 update만!
            TossTransaction transaction = optional.get();
            transaction.setTossStatus(status);
            transaction.setPayMethod(payMethod);
            transaction.setCardCompany(cardCompany);
            transaction.setBankCode(bankCode);
            transaction.setApprovedAt(LocalDateTime.now());
            tossrepository.save(transaction);
        } else {
            // 신규 결제라면 insert
            TossTransaction transaction = TossTransaction.builder()
                    .orderNo(orderNo)
                    .tossStatus(status)
                    .payMethod(payMethod)
                    .cardCompany(cardCompany)
                    .bankCode(bankCode)
                    .approvedAt(LocalDateTime.now())
                    .build();
            tossrepository.save(transaction);
        }
    }
}