package com.reboot.payment.repository;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.payment.entity.Payment;
import com.reboot.auth.entity.Member;
import com.reboot.reservation.entity.Reservation;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 MySQL 사용
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private LectureRepository lectureRepository;

    private Member createSampleMember() {
        return Member.builder()
                .username("testuser" + System.currentTimeMillis())
                .password("pw1234")
                .name("테스터")
                .nickname("테스트닉")
                .email("email" + System.currentTimeMillis() + "@test.com")
                .profileImage(null)
                .phone("010-1234-5678")
                .role("STUDENT")
                .build();
    }

    private Instructor createSampleInstructor(Member member) {
        return Instructor.builder()
                .member(member)
                .career("Java Instructor")
                .description("Experienced Java instructor")
                .reviewNum(0)
                .rating(4.5)
                .build();
    }

    private Lecture createSampleLecture() {
        return Lecture.builder()
                .info(LectureInfo.builder()
                        .title("기초 자바")
                        .description("자바 기초 강의입니다")
                        .price(BigDecimal.valueOf(1000.1))
                        .gameType("lol")
                        .build())
                .metadata(null) // 필요시 값 채우기
                .instructor(null) // 필요시 값 채우기
                .build();
    }

    @Test
    @DisplayName("Payment 저장 및 조회")
    void saveAndFindById() {
        // 샘플 Member 저장
        Member member = memberRepository.save(createSampleMember());
        // 샘플 Instructor 저장
        Instructor instructor = instructorRepository.save(createSampleInstructor(member));
        // 샘플 Lecture 저장
        Lecture lecture = lectureRepository.save(createSampleLecture());

        // Reservation 저장
        Reservation reservation = Reservation.builder()
                .member(member)
                .instructor(instructor)
                .lecture(lecture)
                .date(LocalDateTime.now())
                .status("RESERVED")
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);

        // Payment 저장
        Payment payment = Payment.builder()
                .reservation(savedReservation)
                .price(35000)
                .paymentAt(LocalDateTime.now())
                .method("CARD")
                .status("READY")
                .build();

        Payment saved = paymentRepository.save(payment);
        Optional<Payment> found = paymentRepository.findById(saved.getPaymentId());

        // 값 확인 (디버그 체크)
        System.out.println("Saved Payment ID: " + saved.getPaymentId());
        System.out.println("Saved Price: " + saved.getPrice());
        System.out.println("Saved Method: " + saved.getMethod());
        System.out.println("Saved Status: " + saved.getStatus());

        assertThat(found).isPresent();
        assertThat(found.get().getPrice()).isEqualTo(35000);
        assertThat(found.get().getMethod()).isEqualTo("CARD");
        assertThat(found.get().getStatus()).isEqualTo("READY");
    }

    @Test
    @DisplayName("Payment 삭제")
    void deleteById() {
        // 샘플 Member 저장
        Member member = memberRepository.save(createSampleMember());
        // 샘플 Instructor 저장
        Instructor instructor = instructorRepository.save(createSampleInstructor(member));
        // 샘플 Lecture 저장
        Lecture lecture = lectureRepository.save(createSampleLecture());

        // Reservation 저장
        Reservation reservation = Reservation.builder()
                .member(member)
                .instructor(instructor)
                .lecture(lecture)
                .date(LocalDateTime.now())
                .status("RESERVED")
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);

        // Payment 저장 및 삭제
        Payment payment = Payment.builder()
                .reservation(savedReservation)
                .price(10000)
                .paymentAt(LocalDateTime.now())
                .method("TOSS_MONEY")
                .status("COMPLETE")
                .build();

        Payment saved = paymentRepository.save(payment);
        Long id = saved.getPaymentId();

        System.out.println("Delete Test Payment ID: " + id);

        paymentRepository.deleteById(id);

        Optional<Payment> found = paymentRepository.findById(id);
        assertThat(found).isEmpty();
    }
}