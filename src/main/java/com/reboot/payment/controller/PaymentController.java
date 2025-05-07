package com.reboot.payment.controller;

import com.reboot.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 요청 폼 (reservationId를 쿼리로 받음)
    @GetMapping("/form")
    public String paymentForm(@RequestParam Long reservationId, Model model) {
        model.addAttribute("reservationId", reservationId);
        return "payment/payment-form";
    }

    // 결제 요청 처리 (checkoutPage로 바로 리다이렉트)
    @PostMapping("/request")
    public String requestPayment(@RequestParam Long reservationId) {
        String checkoutPageUrl = paymentService.requestTossPayment(reservationId);
        // 결제창으로 바로 리다이렉트
        return "redirect:" + checkoutPageUrl;
    }

    // 결제 성공 처리
    @GetMapping("/success")
    public String paymentSuccess(
            @RequestParam String status,              // 인증 결과
            @RequestParam(required = false) String orderNo,   // 주문번호
            @RequestParam(required = false) String payMethod, // 결제수단
            @RequestParam(required = false) String cardCompany, // 카드사 코드 (카드 결제시)
            @RequestParam(required = false) String bankCode,   // 은행 코드 (토스머니 결제시)
            @RequestParam(required = false) String paymentKey, // 결제 성공 시 결제키
            @RequestParam(required = false) Integer amount,    // 결제 금액
            Model model
    ) {
        System.out.println("결제 결과: status=" + status +
                ", orderNo=" + orderNo +
                ", payMethod=" + payMethod +
                ", cardCompany=" + cardCompany +
                ", bankCode=" + bankCode);

        // 페이지에 모델값 전달
        model.addAttribute("status", status);
        model.addAttribute("orderNo", orderNo);
        model.addAttribute("payMethod", payMethod);
        model.addAttribute("cardCompany", cardCompany);
        model.addAttribute("bankCode", bankCode);

        // 결제 승인/완료 상태 체크
        if ("PAY_COMPLETE".equals(status) || "PAY_APPROVED".equals(status)) {
            try {
                // Payment 상태 및 TossTransaction 모두 반영
                paymentService.processPaymentSuccess(paymentKey, orderNo, amount);

                // 기타 결제 상세 내역도 업데이트
                paymentService.saveTossTransaction(orderNo, status, payMethod, cardCompany, bankCode);

                model.addAttribute("message", "결제 정보가 정상 저장되었습니다.");
            } catch (Exception e) {
                model.addAttribute("errorMessage", "DB 저장 중 오류: " + e.getMessage());
            }
        } else {
            model.addAttribute("errorMessage", "결제 상태가 완료가 아닙니다.");
        }

        return "payment/payment-success";
    }

    // 결제 취소 처리
    @GetMapping("/cancel")
    public String paymentCancel(
            @RequestParam(required = false) String message,
            @RequestParam(required = false) Long paymentId,
            Model model) {

        model.addAttribute("message", message);
        return "payment/payment-cancel";
    }

    // 토스 결제 콜백 처리
    @PostMapping("/callback")
    @ResponseBody
    public String tossCallback(@RequestBody String callbackData) {
        // 콜백 데이터 처리 로직 구현
        // 실제 구현 시에는 callbackData를 파싱하여 처리
        return "OK";
    }
}