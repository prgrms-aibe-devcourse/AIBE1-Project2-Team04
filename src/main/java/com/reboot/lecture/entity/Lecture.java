package com.reboot.lecture.entity;

import com.reboot.instructor.entity.Instructor;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lecture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long lectureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "game_type")
    private String gameType;

    @Column(name = "price")
    private Integer price;
}