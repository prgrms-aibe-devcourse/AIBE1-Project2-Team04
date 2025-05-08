### 개인 작업 패키지 구조
com.reboot  
├── auth  
│   ├── entity  
│   │   ├── Instructor.java (강사 정보 엔티티)   
│   ├── repository  
│   │   ├── InstructorRepository.java (강사 데이터 접근 인터페이스)  
│   ├── service  
│   │   └── AuthService.java (인증 및 사용자 정보 서비스)  
│   └── exception  
│       └── InstructorNotFoundException.java (강사 정보 조회 실패 예외)  
└── lecture  
├── entity  
│   └── Lecture.java (강의 정보 엔티티)  
├── repository  
│   └── LectureRepository.java (강의 데이터 접근 인터페이스)  
├── service  
│   ├── LectureService.java (강의 조회 서비스 인터페이스)  
│   ├── LectureServiceImpl.java (강의 조회 서비스 구현체)  
│   ├── InstructorLectureService.java (강사용 강의 관리 서비스 인터페이스)  
│   └── InstructorLectureServiceImpl.java (강사용 강의 관리 서비스 구현체)  
├── controller  
│   ├── LectureController.java (일반 사용자용 강의 조회 컨트롤러)  
│   └── InstructorLectureController.java (강사용 강의 관리 컨트롤러)  
│   ├── LectureTestController.java (일반 사용자용 강의 조회 Test 컨트롤러)  
│   └── InstructorLectureTestController.java (강사용 강의 관리 Test 컨트롤러)  
├── dto  
│   ├── LectureDto.java (강의 정보 DTO)  
│   ├── LectureRequest.java (강의 생성/수정 요청 DTO)  
│   └── LectureResponse.java (강의 응답 DTO)  
└── exception  
├── LectureNotFoundException.java (강의 정보 조회 실패 예외)  
└── UnauthorizedLectureAccessException.java (권한 없는 강의 접근 예외)  

---

### PR 요청 템플릿 (리뷰어 2명 선정)

```
## 📌 PR 요약
> 이번 PR에서 작업한 핵심 변경사항을 간단히 작성해주세요.

- 예시: 회원가입 기능 구현 (User 엔티티 + AuthController 추가)

---

## ⚒️ 작업 및 변경 내용 상세
> 어떤 작업을 했는지, 어떤 파일이 변경되었는지 구체적으로 작성해주세요.

- User.java, UserService.java, AuthController.java 파일 추가
- 회원가입 요청/응답 DTO 작성
- Spring Security 기본 설정 (SecurityConfig.java) 수정

---

## 🔥 리뷰 요청 포인트
> 리뷰어가 집중해서 봐야 할 부분이나, 확신이 없는 코드가 있으면 알려주세요.

- 비밀번호 암호화 로직 적절한지 확인 요청
- 에러 핸들링 방식 리뷰 요청

```

---

### 커밋 컨벤션

| 타입 | 의미 | 예시 |
|:----|:----|:----|
| feat | 새로운 기능 추가 | feat: 회원가입 API 구현 |
| fix | 버그 수정 | fix: 로그인 실패 시 에러 반환 수정 |
| docs | 문서 수정 (README, 주석 등) | docs: ERD 다이어그램 추가 |
| style | 코드 스타일 수정 (세미콜론, 들여쓰기 등) | style: 코딩 컨벤션 적용 |
| refactor | 리팩토링 (기능 변경 없이 구조 개선) | refactor: UserService 로직 정리 |
| test | 테스트 코드 추가/수정 | test: UserService 단위 테스트 추가 |
| chore | 기타 잡다한 작업 (빌드 설정 등) | chore: .gitignore 파일 업데이트 |
| perf | 성능 개선 | perf: 쿼리 최적화 처리 |

---

### 브랜치 구조(개별 브랜치 - 파트 브랜치 - dev - main)

![스크린샷 2025-04-29 102516](https://github.com/user-attachments/assets/a97102e9-30f8-415e-9d0e-36ef242fa0cc)