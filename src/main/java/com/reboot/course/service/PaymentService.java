//package com.reboot.course.service;
//
//import com.reboot.course.dto.PaymentRequestDto;
//import com.reboot.course.dto.PaymentResponseDto;
//import com.reboot.course.entity.Reservation;
//import com.reboot.course.repository.ReservationRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//public class PaymentService {
//    /**
//     * 결제(Payment) 도메인의 비즈니스 로직을 담당하는 서비스 클래스
//     */
//    @Service
//    @RequiredArgsConstructor
//    public class PaymentService {
//        private final PaymentRepository paymentRepository;
//        private final ReservationRepository reservationRepository;
//
//        /**
//         * 새로운 결제를 생성한다.
//         * @param dto 결제 요청 DTO (예약ID, 결제방법 등)
//         * @return 생성된 결제의 응답 DTO
//         */
//        @Transactional
//        public PaymentResponseDto createPayment(PaymentRequestDto dto) {
//            // 예약 엔티티 조회 (존재하지 않으면 예외 발생)
//            Reservation reservation = reservationRepository.findById(dto.getReservationId())
//                    .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
//
//            // 이미 결제가 존재하는지 확인
//            Payment existingPayment = paymentRepository.findByReservationReservationId(dto.getReservationId());
//            if (existingPayment != null) {
//                throw new RuntimeException("이미 결제가 완료된 예약입니다.");
//            }
//
//            // 결제 엔티티 생성 및 저장
//            Payment payment = Payment.builder()
//                    .reservation(reservation)
//                    .price(reservation.getLecture().getPrice())
//                    .date(LocalDateTime.now())
//                    .method(dto.getMethod())
//                    .status("결제완료")
//                    .build();
//
//            Payment savedPayment = paymentRepository.save(payment);
//
//            // 예약 상태 업데이트
//            reservation.setStatus("결제완료");
//            reservationRepository.save(reservation);
//
//            // 엔티티를 DTO로 변환하여 반환
//            return convertToDto(savedPayment);
//        }
//
//        /**
//         * 결제 ID로 결제 상세 조회
//         */
//        @Transactional(readOnly = true)
//        public PaymentResponseDto getPayment(Long paymentId) {
//            Payment payment = paymentRepository.findById(paymentId)
//                    .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
//
//            return convertToDto(payment);
//        }
//
//        /**
//         * 예약 ID로 결제 조회
//         */
//        @Transactional(readOnly = true)
//        public PaymentResponseDto getPaymentByReservation(Long reservationId) {
//            Payment payment = paymentRepository.findByReservationReservationId(reservationId);
//            if (payment == null) {
//                throw new RuntimeException("결제 내역이 없습니다.");
//            }
//
//            return convertToDto(payment);
//        }
//
//        /**
//         * 결제 환불 처리
//         */
//        @Transactional
//        public PaymentResponseDto refundPayment(Long paymentId) {
//            Payment payment = paymentRepository.findById(paymentId)
//                    .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
//
//            payment.setStatus("환불완료");
//            Payment savedPayment = paymentRepository.save(payment);
//
//            // 예약 상태 업데이트
//            Reservation reservation = payment.getReservation();
//            reservation.setStatus("환불완료");
//            reservationRepository.save(reservation);
//
//            return convertToDto(savedPayment);
//        }
//
//        /**
//         * Payment 엔티티를 PaymentResponseDto로 변환
//         */
//        private PaymentResponseDto convertToDto(Payment payment) {
//            return PaymentResponseDto.builder()
//                    .paymentId(payment.getPaymentId())
//                    .reservationId(payment.getReservation().getReservationId())
//                    .price(payment.getPrice())
//                    .date(payment.getDate())
//                    .method(payment.getMethod())
//                    .status(payment.getStatus())
//                    .build();
//        }
//    }
//}
