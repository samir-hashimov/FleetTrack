# 1. BUILD MƏRHƏLƏSİ
FROM gradle:8.7-jdk21-alpine AS builder
WORKDIR /app

# Asılılıqları daha sürətli yükləmək üçün əvvəlcə build fayllarını kopyalayırıq
COPY build.gradle settings.gradle ./
COPY src src

# Proyekti testlərsiz build edirik (.jar faylını yaradırıq)
RUN gradle clean build -x test

# 2. RUN MƏRHƏLƏSİ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Birinci mərhələdə yaranan .jar faylını bura kopyalayırıq
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

# Konteynerin 8080 portunu açırıq
EXPOSE 8080

# Proyekti işə salırıq
ENTRYPOINT ["java", "-jar", "app.jar"]