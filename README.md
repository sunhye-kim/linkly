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

## 🚀 빠른 시작 (Quick Start)

### 사전 요구사항
- Docker & Docker Compose
- Node.js & npm

### 1. 환경 변수 설정

`.env.example` 파일을 복사하여 `.env` 파일 생성:

```bash
cp .env.example .env
```

필요시 `.env` 파일에서 데이터베이스 비밀번호 변경:
```env
MYSQL_ROOT_PASSWORD=your_password
DB_PASSWORD=your_password
```

### 2. 프론트엔드 의존성 설치

```bash
cd frontend
npm install
cd ..
```

### 3. Docker로 서비스 실행

```bash
docker compose up -d --build
```

### 4. 실행 확인

```bash
# 컨테이너 상태 확인
docker compose ps

# 로그 확인
docker compose logs -f app
```

### 접속 정보

- **백엔드 API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **프론트엔드 개발 서버**:
  ```bash
  cd frontend
  npm run dev
  # http://localhost:3000
  ```
- **MySQL**: localhost:3306 (root/{설정한 비밀번호})

### Docker 관리 명령어

```bash
# 서비스 시작
docker compose up -d

# 서비스 중지
docker compose down

# 서비스 중지 + 볼륨 삭제 (데이터베이스 초기화)
docker compose down -v

# 로그 확인
docker compose logs -f app
```

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