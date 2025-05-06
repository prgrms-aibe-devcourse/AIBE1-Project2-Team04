package com.reboot.payment.controller;

public class PaymentController {
}
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/payment")
//
//public class PaymentController {
//    private final PaymentService paymentService;
//    private final ReservationService reservationService;
//
//    /**
//     * 결제 폼 페이지 진입
//     * GET /payment/new?reservationId=xxx
//     */
//    @GetMapping("/new")
//    public String paymentForm(@RequestParam Long reservationId, Model model) {
//        model.addAttribute("reservation", reservationService.getReservation(reservationId));
//        model.addAttribute("paymentRequestDto", new PaymentRequestDto());
//        return "payment/paymentForm";
//    }
//
//    /**
//     * 결제 요청 처리
//     * POST /payment
//     */
//    @PostMapping
//    public String createPayment(@ModelAttribute PaymentRequestDto dto, Model model) {
//        PaymentResponseDto payment = paymentService.createPayment(dto);
//        model.addAttribute("payment", payment);
//        return "payment/paymentResult";
//    }
//
//    /**
//     * 결제 상세 조회
//     * GET /payment/{id}
//     */
//    @GetMapping("/{id}")
//    public String getPayment(@PathVariable Long id, Model model) {
//        PaymentResponseDto payment = paymentService.getPayment(id);
//        model.addAttribute("payment", payment);
//        return "payment/paymentResult";
//    }
//
//    /**
//     * 결제 환불 요청 처리
//     * POST /payment/{id}/refund
//     */
//    @PostMapping("/{id}/refund")
//    public String refundPayment(@PathVariable Long id, Model model) {
//        PaymentResponseDto payment = paymentService.refundPayment(id);
//        model.addAttribute("payment", payment);
//        return "payment/refundResult";
//    }
//}