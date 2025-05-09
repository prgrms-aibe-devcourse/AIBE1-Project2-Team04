/*
package com.reboot.survey.service;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.repository.LectureRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.reboot.survey.dto.Document;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataPreparationService {

    private final LectureRepository lectureRepository;
    private final InstructorRepository instructorRepository;
    private final VectorDbClient vectorDbClient;
    private final EmbeddingService embeddingService;
    private final Logger log = LoggerFactory.getLogger(DataPreparationService.class);

    @Autowired
    public DataPreparationService(
            LectureRepository lectureRepository,
            InstructorRepository instructorRepository,
            VectorDbClient vectorDbClient,
            EmbeddingService embeddingService) {
        this.lectureRepository = lectureRepository;
        this.instructorRepository = instructorRepository;
        this.vectorDbClient = vectorDbClient;
        this.embeddingService = embeddingService;
    }

    @PostConstruct
    public void initializeVectorDb() {
        List<Lecture> allLectures = lectureRepository.findAll();

        List<Document> documents = prepareLectureDocuments(allLectures);

        // 각 문서를 임베딩하고 벡터 DB에 저장
        for (Document document : documents) {
            float[] embedding = embeddingService.generateEmbedding(document.getContent());
            vectorDbClient.upsert(document.getMetadata().get("lectureId").toString(), embedding, document.getMetadata());
        }

        log.info("벡터 DB 초기화 완료: {} 개의 강의 데이터 인덱싱됨", documents.size());
    }

    // 강의 정보를 문서화하는 메서드
    private List<Document> prepareLectureDocuments(List<Lecture> lectures) {
        List<Document> documents = new ArrayList<>();

        for (Lecture lecture : lectures) {
            Instructor instructor = lecture.getInstructor();
            Member instructorMember = instructor.getMember();

            // 강의 문서 생성
            StringBuilder documentContent = new StringBuilder();
            documentContent.append("강의 ID: ").append(lecture.getId()).append("\n");
            documentContent.append("제목: ").append(lecture.getInfo().getTitle()).append("\n");
            documentContent.append("게임 타입: ").append(lecture.getInfo().getGameType()).append("\n");
            documentContent.append("설명: ").append(lecture.getInfo().getDescription()).append("\n");
            documentContent.append("가격: ").append(lecture.getInfo().getPrice()).append("\n");
            documentContent.append("강의 시간: ").append(lecture.getInfo().getDuration()).append("\n");
            documentContent.append("랭크/티어: ").append(lecture.getInfo().getRank_()).append("\n");
            documentContent.append("포지션: ").append(lecture.getInfo().getPosition()).append("\n");
            documentContent.append("평균 평점: ").append(lecture.getMetadata().getAverageRating()).append("\n");
            documentContent.append("수강생 수: ").append(lecture.getMetadata().getTotalMembers()).append("\n");
            documentContent.append("리뷰 수: ").append(lecture.getMetadata().getReviewCount()).append("\n");
            documentContent.append("강사 ID: ").append(instructor.getInstructorId()).append("\n");
            documentContent.append("강사 이름: ").append(instructorMember.getName()).append("\n");
            documentContent.append("강사 닉네임: ").append(instructorMember.getNickname()).append("\n");
            documentContent.append("강사 경력: ").append(instructor.getCareer()).append("\n");
            documentContent.append("강사 설명: ").append(instructor.getDescription()).append("\n");
            documentContent.append("강사 평점: ").append(instructor.getRating()).append("\n");
            documentContent.append("강사 리뷰 수: ").append(instructor.getReviewNum()).append("\n");

            // 메타데이터 생성
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("lectureId", lecture.getId().toString());
            metadata.put("gameType", lecture.getInfo().getGameType());
            metadata.put("instructorId", String.valueOf(instructor.getInstructorId()));
            metadata.put("price", lecture.getInfo().getPrice().toString());
            metadata.put("rank", lecture.getInfo().getRank_());
            metadata.put("position", lecture.getInfo().getPosition());
            metadata.put("rating", String.valueOf(lecture.getMetadata().getAverageRating()));
            metadata.put("isActive", String.valueOf(lecture.getMetadata().isActive()));

            Document document = new Document(documentContent.toString(), metadata);
            documents.add(document);
        }

        return documents;
    }

    // 새 강의가 추가되면 벡터 DB 업데이트
    @EventListener
    public void handleLectureCreatedEvent(LectureCreatedEvent event) {
        Lecture lecture = event.getLecture();

        List<Document> documents = prepareLectureDocuments(List.of(lecture));

        for (Document document : documents) {
            float[] embedding = embeddingService.generateEmbedding(document.getContent());
            vectorDbClient.upsert(document.getMetadata().get("lectureId").toString(), embedding, document.getMetadata());
        }

        log.info("새 강의가 벡터 DB에 추가되었습니다. 강의 ID: {}", lecture.getId());
    }
}

*/
