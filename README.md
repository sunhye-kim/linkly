# Linkly

Spring Boot 기반 API 서버 프로젝트

## 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.7
- **Build Tool**: Gradle 8.x
- **Database**: MySQL 8.0 (운영), H2 (로컬 개발)
- **ORM**: Spring Data JPA (Hibernate)
- **Containerization**: Docker & Docker Compose

## 프로젝트 구조

```
src/main/java/com/example/linkly/
├── controller/      # REST API 엔드포인트
├── service/         # 비즈니스 로직
├── repository/      # 데이터 접근 계층 (JPA Repository)
├── domain/          # JPA 엔티티
├── dto/             # 데이터 전송 객체
└── LinklyApplication.java
```

## 사전 요구사항

### 로컬 개발
- Java 17 이상
- Gradle 8.x (또는 gradlew 사용)

### Docker 실행
- Docker
- Docker Compose

## 실행 방법

### 1. 로컬 환경 (H2 Database)

프로젝트 클론 후:

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

**접속 정보:**
- API 서버: http://localhost:8080
- H2 콘솔: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (공백)

### 2. Docker 환경 (MySQL Database)

**환경 설정 (최초 1회):**

```bash
# .env 파일 생성
cp .env.example .env

# .env 파일을 열어서 비밀번호 등 설정값 수정
# DB_PASSWORD, MYSQL_ROOT_PASSWORD 등을 변경하세요
```

**실행:**

```bash
# Docker 컨테이너 빌드 및 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 종료
docker-compose down

# 볼륨까지 삭제 (DB 데이터 초기화)
docker-compose down -v
```

**접속 정보:**
- API 서버: http://localhost:8080
- MySQL:
  - Host: localhost
  - Port: 3306
  - Database: linkly
  - Username: root
  - Password: root

## 데이터베이스 설정

### 프로파일

- **local**: H2 인메모리 데이터베이스 (개발용)
- **prod**: MySQL 데이터베이스 (운영용)

### 환경변수 설정

프로젝트는 `.env` 파일을 통해 환경변수를 관리합니다.

**설정 방법:**

1. `.env.example` 파일을 복사하여 `.env` 파일 생성
   ```bash
   cp .env.example .env
   ```

2. `.env` 파일에서 필요한 값 수정
   ```bash
   # 예시: 비밀번호 변경
   DB_PASSWORD=your_secure_password
   MYSQL_ROOT_PASSWORD=your_secure_password
   ```

**주의사항:**
- `.env` 파일은 Git에 커밋되지 않습니다 (`.gitignore`에 포함)
- 운영 환경에서는 반드시 강력한 비밀번호로 변경하세요
- `.env.example`은 템플릿 파일로 Git에 포함됩니다

### 환경변수 목록

| 변수명 | 설명 | 기본값 (.env) |
|--------|------|---------------|
| SPRING_PROFILES_ACTIVE | 활성 프로파일 | prod |
| DB_HOST | MySQL 호스트 | mysql |
| DB_PORT | MySQL 포트 | 3306 |
| DB_NAME | 데이터베이스 이름 | linkly |
| DB_USERNAME | 데이터베이스 사용자 | root |
| DB_PASSWORD | 데이터베이스 비밀번호 | root (변경 필요) |
| MYSQL_ROOT_PASSWORD | MySQL root 비밀번호 | root (변경 필요) |
| MYSQL_DATABASE | MySQL 데이터베이스 이름 | linkly |

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```

## 빌드

```bash
# JAR 파일 생성
./gradlew build

# 테스트 스킵하고 빌드
./gradlew build -x test

# 빌드 결과물 위치
# build/libs/Linkly-0.0.1-SNAPSHOT.jar
```

## API 문서

_추후 추가 예정 (Swagger/OpenAPI)_

## 개발 환경 설정

### IntelliJ IDEA 설정

1. **프로젝트 열기**: `File > Open` → `build.gradle` 선택
2. **SDK 설정**: `File > Project Structure > Project` → Java 17 설정
3. **Gradle 새로고침**: Gradle 탭에서 새로고침 버튼 클릭
4. **Lombok 플러그인 설치**: `Settings > Plugins` → "Lombok" 검색 및 설치
5. **Annotation Processing 활성화**:
   - `Settings > Build > Compiler > Annotation Processors`
   - "Enable annotation processing" 체크
