# FleetTrack

**FleetTrack** — nəqliyyat parkının (fleet) idarəolunması üçün REST API və real-vaxt izləmə backend-i. Tətbiq vasitəsilə nəqliyyat vasitələri, sürücülər, texniki xidmət qeydləri, GPS koordinatları və park hesabatları idarə olunur. Autentifikasiya JWT ilə həyata keçirilir; canlı xəritə və xəbərdarlıqlar WebSocket + Redis pub/sub üzərindən ötürülür.

Versiya: `0.0.1-SNAPSHOT` · API: `v1.0.0` · Context path: `/api`

---

## Mündəricat

1. [Nə edir](#nə-edir)
2. [Texnologiya steki](#texnologiya-steki)
3. [Arxitektura](#arxitektura)
4. [Layihə strukturu](#layihə-strukturu)
5. [Rollar və icazələr](#rollar-və-icazələr)
6. [Verilənlər modeli](#verilənlər-modeli)
7. [Tələblər](#tələblər)
8. [Konfiqurasiya (.env)](#konfiqurasiya-env)
9. [İşə salma](#işə-salma)
10. [Swagger / OpenAPI](#swagger--openapi)
11. [Autentifikasiya](#autentifikasiya)
12. [REST API](#rest-api)
13. [WebSocket (GPS və xəbərdarlıqlar)](#websocket-gps-və-xəbərdarlıqlar)
14. [Redis: keş və xəbərdarlıqlar](#redis-keş-və-xəbərdarlıqlar)
15. [Planlaşdırıcılar (schedulers)](#planlaşdırıcılar-schedulers)
16. [Rate limiting](#rate-limiting)
17. [Xəta formatı](#xəta-formatı)
18. [Testlər](#testlər)
19. [Təhlükəsizlik qeydləri](#təhlükəsizlik-qeydləri)

---

## Nə edir

Sistem aşağıdakı biznes prosesləri əhatə edir:

| Funksiya | Qısa izah |
| --- | --- |
| İstifadəçi və rollər | İctimai qeydiyyat yalnız `DRIVER` yaradır. `ADMIN` və `FLEET_MANAGER` hesablarını yalnız mövcud admin yaradır. |
| Nəqliyyat vasitələri | Marka, model, il, nömrə nişanı, status, GPS. Filtr, sıralama, səhifələmə. |
| Sürücülər | Profil, əlaqə məlumatı, istifadəçi hesabı və vasitəyə 1:1 təyinat. Soft-delete (`BLOCKED`). |
| Texniki xidmət | Planlaşdırılmış işlər, tamamlanma statusu, vasitəyə görə siyahı. |
| GPS izləmə | Koordinat yeniləməsi REST və ya STOMP ilə; canlı yayım `/topic/vehicle-locations`. |
| Park xəbərdarlıqları | Offline vasitə, yaxınlaşan və gecikmiş servis — Redis kanalı → WebSocket. |
| PDF hesabat | Park statusu və texniki xidmət cədvəli (`OpenPDF`). |
| Keş | Vasitə və sürücü xülasələri Redis-də (TTL 300 saniyə). |
| Rate limit | Bucket4j: dəqiqədə 100 sorğu (istifadəçi və ya IP). |

---

## Texnologiya steki

| Komponent | Texnologiya |
| --- | --- |
| Dil / runtime | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Build | Gradle 8 (wrapper), `io.spring.dependency-management` 1.1.7 |
| Web | Spring Web (Servlet), context path `/api`, port `8080` |
| Təhlükəsizlik | Spring Security, JWT (JJWT 0.12.6), BCrypt, stateless sessiyalar |
| ORM | Spring Data JPA / Hibernate (`ddl-auto=validate`) |
| Miqrasiya | Flyway (`classpath:db/migration`) |
| Verilənlər bazası | PostgreSQL 15 (runtime), H2 (test) |
| Keş / pub-sub | Redis 7, Spring Data Redis, Lettuce pool |
| Real-vaxt | Spring WebSocket + STOMP + SockJS |
| Mapping | MapStruct 1.6.3 (`componentModel=spring`) |
| Validasiya | Jakarta Bean Validation |
| API sənədi | springdoc-openapi 2.8.6 (Swagger UI) |
| Rate limit | Bucket4j 8.10.1 |
| PDF | OpenPDF 2.0.3 |
| Digər | Lombok, spring-dotenv 4.0.0 |
| Konteynerləşdirmə | Docker multi-stage (Gradle 8.7 JDK 21 → Temurin 21 JRE Alpine), Docker Compose |

---

## Arxitektura

Tətbiq klassik **layered** Spring arxitekturasındadır: