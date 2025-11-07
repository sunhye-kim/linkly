# Linkly

북마크 관리 애플리케이션

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.5.7
- Spring Data JPA
- MySQL / H2 Database
- Lombok
- Swagger/OpenAPI

### Frontend
- React 18
- Vite
- JavaScript/JSX

## 프로젝트 구조

```
Linkly/
├── src/                        # Spring Boot 백엔드
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │       └── static/         # 프론트엔드 빌드 결과물
│   └── test/
├── frontend/                   # React 프론트엔드
│   └── (프론트엔드 README 참고)
├── build.gradle
└── README.md
```

## 백엔드 실행 방법

### 1. 사전 요구사항
- Java 17 이상
- MySQL (또는 H2 사용)

### 2. 개발 모드 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 IDE에서 LinklyApplication 실행
```

서버가 시작되면: http://localhost:8080

### 3. API 문서 확인

Swagger UI: http://localhost:8080/swagger-ui.html

### 4. 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 테스트만
./gradlew test --tests "com.linkly.bookmark.*"
```

### 5. 빌드

```bash
# JAR 파일 생성
./gradlew build

# 생성된 JAR 파일 실행
java -jar build/libs/Linkly-0.0.1-SNAPSHOT.jar
```

## 코드 포맷팅

프로젝트는 Spotless를 사용하여 코드 스타일을 관리합니다.

```bash
# 코드 포맷 체크
./gradlew spotlessCheck

# 자동 포맷 적용
./gradlew spotlessApply
```

## 프론트엔드 + 백엔드 통합 실행

### 개발 모드 (권장)

**터미널 1 - 백엔드:**
```bash
./gradlew bootRun
```

**터미널 2 - 프론트엔드:**
```bash
cd frontend
npm run dev
```

- 백엔드: http://localhost:8080
- 프론트엔드: http://localhost:3000
- API 요청은 자동으로 프록시됨

### 프로덕션 빌드

```bash
# 1. 프론트엔드 빌드
cd frontend
npm run build

# 2. 백엔드 빌드 (프론트엔드 포함)
cd ..
./gradlew build

# 3. 실행
java -jar build/libs/Linkly-0.0.1-SNAPSHOT.jar
```

단일 JAR 파일로 프론트+백엔드 모두 실행: http://localhost:8080

## API 엔드포인트

### 사용자 (User)
- `POST /api/users` - 회원 가입
- `GET /api/users/{id}` - 사용자 조회
- `GET /api/users` - 전체 사용자 조회
- `PUT /api/users/{id}` - 사용자 정보 수정
- `DELETE /api/users/{id}` - 사용자 삭제

### 카테고리 (Category)
- `POST /api/categories` - 카테고리 생성
- `GET /api/categories/{id}` - 카테고리 조회
- `GET /api/categories/user/{userId}` - 사용자별 카테고리 조회
- `PUT /api/categories/{id}` - 카테고리 수정
- `DELETE /api/categories/{id}` - 카테고리 삭제

### 북마크 (Bookmark)
- `POST /api/bookmarks` - 북마크 생성
- `GET /api/bookmarks/{id}` - 북마크 조회
- `GET /api/bookmarks/user/{userId}` - 사용자별 북마크 조회
- `PUT /api/bookmarks/{id}` - 북마크 수정
- `DELETE /api/bookmarks/{id}` - 북마크 삭제

## 데이터베이스 설정

### H2 (개발용)
`application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
```

H2 콘솔: http://localhost:8080/h2-console

### MySQL (프로덕션)
`application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/linkly
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## 환경 변수

`.env` 파일 생성 (선택사항):
```
DB_URL=jdbc:mysql://localhost:3306/linkly
DB_USERNAME=root
DB_PASSWORD=password
```

## 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 라이센스

MIT License