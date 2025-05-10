package com.reboot.payment.controller;

import com.reboot.payment.entity.Payment;
import com.reboot.payment.entity.RefundHistory;
import com.reboot.payment.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/refunds")
public class RefundController {

    private final RefundService refundService;

    // 환불 폼 페이지 표시
    @GetMapping("/form")
    public String refundForm(@RequestParam Long paymentId, Model model) {
        Payment payment = refundService.getPayment(paymentId);
        model.addAttribute("payment", payment);
        return "payment/refund-form";
    }

    // 환불 처리
    @PostMapping("/process")
    public String processRefund(
            @RequestParam Long paymentId,
            @RequestParam(required = false) Integer amount,
            @RequestParam(required = false) String reason,
            Model model) {
        try {
            RefundHistory refundHistory = refundService.processRefundWithHistory(paymentId, amount, reason);

            if ("REFUND_COMPLETED".equals(refundHistory.getStatus())) {
                model.addAttribute("message", "환불이 성공적으로 처리되었습니다.");
            } else {
                model.addAttribute("errorMessage", "환불 처리 중 오류가 발생했습니다: " + refundHistory.getResponseMessage());
            }

            model.addAttribute("refundNo", refundHistory.getRefundNo());
            model.addAttribute("status", refundHistory.getStatus());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "환불 처리 중 오류: " + e.getMessage());
        }
        return "payment/refund-result";
    }

    // 환불 번호로 환불 내역 상세 조회
    @GetMapping("/history/refund/{refundNo}")
    public String getRefundHistoryDetail(@PathVariable String refundNo, Model model) {
        try {
            RefundHistory refundHistory = refundService.getRefundHistoryByRefundNo(refundNo);
            model.addAttribute("refundHistory", refundHistory);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "환불 내역 조회 중 오류: " + e.getMessage());
        }
        return "payment/refund-detail";
    }
}