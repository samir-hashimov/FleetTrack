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
| İstifadəçi və rollar | İctimai qeydiyyat yalnız `DRIVER` yaradır. `ADMIN` və `FLEET_MANAGER` hesablarını yalnız mövcud admin yaradır. |
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

```
Client (HTTP / SockJS)
        │
        ▼
┌───────────────────────────────────────┐
│  Controllers  +  WebSocket endpoints  │
│  JWT filter, CORS, RateLimit interceptor │
└───────────────────┬───────────────────┘
                    ▼
┌───────────────────────────────────────┐
│  Services  (biznes qaydaları, keş)    │
└───────────────────┬───────────────────┘
                    ▼
┌──────────────┬──────────────┬─────────┐
│ JPA / Flyway │ Redis cache  │ Redis   │
│ PostgreSQL   │ summaries    │ pub/sub │
└──────────────┴──────────────┴─────────┘
```

**GPS axını**

1. Sürücü və ya menecer koordinat göndərir: `POST /api/vehicle-locations` və ya STOMP `/app/gps`.
2. `VehicleLocationService` vasitəni yoxlayır, koordinatı saxlayır; status `OFFLINE` idisə `ACTIVE` olur.
3. `VehicleLocationBroadcaster` mesajı `/topic/vehicle-locations` mövzusuna göndərir.

**Xəbərdarlıq axını**

1. Vasitə offline işarələnir və ya `MaintenanceScheduler` yaxın/gecikmiş servis tapır.
2. `FleetAlertPublisher` JSON-u Redis kanalına (`fleet-alerts`) yazır.
3. `FleetAlertSubscriber` oxuyur və `FleetAlertWebSocketBroadcaster` vasitəsilə `/topic/fleet-alerts` üzərinə yayır.

Bu model eyni Redis-ə qoşulmuş bir neçə app instansiyasında xəbərdarlıqların hamısına çatmasına imkan verir.

---

## Layihə strukturu

```
FleetTrack/
├── build.gradle                 # Asılılıqlar və Java 21 toolchain
├── settings.gradle
├── docker-compose.yml           # app + postgres + redis
├── Dockerfile                   # Multi-stage jar build
├── .env.example                 # Mühit dəyişənləri şablonu
├── src/main/java/com/fleettrack/
│   ├── FleetTrackApplication.java
│   ├── config/                  # Security, Redis, Cache, CORS, OpenAPI, WebSocket, MVC
│   ├── controller/              # REST endpoint-lər
│   ├── service/                 # Biznes məntiqi
│   ├── dao/entity/              # JPA entity-lər
│   ├── dao/repository/          # Spring Data repository-lər
│   ├── dto/request|response/    # API kontraktları
│   ├── mapper/                  # MapStruct
│   ├── security/                # JWT, UserDetails
│   ├── websocket/               # GPS ingest + broadcast
│   ├── redis/                   # Pub/sub publisher/subscriber
│   ├── scheduler/               # Cron tapşırıqları
│   ├── ratelimit/               # Bucket4j interceptor
│   ├── specification/           # JPA Specifications (vasitə axtarışı)
│   ├── exception/               # GlobalExceptionHandler
│   └── util/                    # Role, VehicleStatus, DriverStatus
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/V1__init_schema.sql
└── src/test/                    # H2 + test profile (Redis/Flyway söndürülür)
```

---

## Rollar və icazələr

Üç rol var (`com.fleettrack.util.Role`):

| Rol | Necə yaranır | Tipik hüquqlar |
| --- | --- | --- |
| `ADMIN` | Seed və ya `POST /admin/users` | Tam CRUD, yüksək səviyyəli istifadəçi yaratmaq, silmə əməliyyatları |
| `FLEET_MANAGER` | Yalnız admin (`POST /admin/users`) | Vasitə/sürücü/servis yaratmaq və yeniləmək, PDF, axtarış; silmə yox |
| `DRIVER` | `POST /auth/register` | Öz profili, özünə təyin olunmuş vasitənin GPS/offline yeniləməsi, vasitəni ID ilə oxumaq |

**Təhlükəsizlik qaydaları (qısa):**

- JWT-siz açıq yollar: `/auth/**`, Swagger, `/ws/**`, `OPTIONS`.
- `/admin/**` yalnız `ADMIN`.
- Digər bütün HTTP sorğuları autentifikasiya tələb edir.
- Metod səviyyəsində `@PreAuthorize` əlavə məhdudiyyət qoyur.
- Sürücü başqa sürücünün profilinə və ya başqasının vasitəsinə toxuna bilməz (`UnauthorizedActionException`).
- `DRIVER` rolunu `/admin/users` ilə yaratmaq olmaz — biznes xətası qaytarılır.

---

## Verilənlər modeli

Flyway miqrasiyası: `src/main/resources/db/migration/V1__init_schema.sql`.

```
users 1:1 drivers 0..1:1 vehicles
                    │
                    └── 1:N maintenance_logs
```

### `users`

| Sahə | Tip | Qeyd |
| --- | --- | --- |
| id | BIGSERIAL | PK |
| username | VARCHAR(100) | unikal |
| password | VARCHAR(255) | BCrypt |
| role | VARCHAR(50) | `ADMIN`, `FLEET_MANAGER`, `DRIVER` |
| created_at / updated_at | TIMESTAMPTZ | |

### `vehicles`

| Sahə | Tip | Qeyd |
| --- | --- | --- |
| id | BIGSERIAL | PK |
| make, model | VARCHAR(100) | |
| year | INTEGER | `1900–2100` |
| license_plate | VARCHAR(20) | unikal |
| status | VARCHAR(50) | aşağıya bax |
| latitude, longitude | DOUBLE PRECISION | GPS |
| created_at / updated_at | TIMESTAMPTZ | |

**Vasitə statusları** (`VehicleStatus`): `ACTIVE`, `IN_MAINTENANCE`, `OFFLINE`, `DECOMMISSIONED`.

### `drivers`

| Sahə | Tip | Qeyd |
| --- | --- | --- |
| id | BIGSERIAL | PK |
| first_name, last_name | VARCHAR(100) | |
| license_number | VARCHAR(50) | unikal |
| email, phone | | `ContactData` embed |
| status | VARCHAR(20) | `ACTIVE` və ya `BLOCKED` |
| user_id | BIGINT UNIQUE | `users` — `ON DELETE SET NULL` |
| vehicle_id | BIGINT UNIQUE | `vehicles` — bir sürücü, bir vasitə |
| created_at / updated_at | TIMESTAMPTZ | |

**Sürücü statusları** (`DriverStatus`): `ACTIVE`, `BLOCKED`.

Admin sürücünü siləndə sətir silinmir: status `BLOCKED` olur, təyin olunmuş vasitə boşaldılır. 30 gün sonra `DriverCleanupScheduler` `BLOCKED` və köhnə `updated_at` olan sətirləri fiziki silir.

### `maintenance_logs`

| Sahə | Tip | Qeyd |
| --- | --- | --- |
| id | BIGSERIAL | PK |
| vehicle_id | BIGINT | `ON DELETE CASCADE` |
| description | TEXT | |
| scheduled_date | DATE | |
| is_completed | BOOLEAN | default `false` |
| created_at / updated_at | TIMESTAMPTZ | |

İndekslər: `vehicles(status)`, `vehicles(year)`, `maintenance_logs(scheduled_date)`, `maintenance_logs(is_completed)`.

Hibernate `ddl-auto=validate` — sxem yalnız Flyway ilə dəyişməlidir.

---

## Tələblər

Lokal işə salmaq üçün:

- **JDK 21**
- **Docker Desktop** (Compose ilə PostgreSQL + Redis üçün tövsiyə olunur) və ya ayrıca PostgreSQL 15 və Redis 7
- Gradle wrapper (`./gradlew` / `gradlew.bat`) — Gradle-i ayrıca quraşdırmaq məcburi deyil

---

## Konfiqurasiya (.env)

`.env.example` faylını kopyalayıb `.env` yaradın. `spring-dotenv` bu dəyərləri `application.properties`-ə ötürür.

```env
# PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/fleettrack_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password_or_leave_blank

# JWT — production-da ən azı 256-bit (32+ simvol) güclü açar
JWT_SECRET=your_super_secret_jwt_key_here_must_be_long
```

**Docker Compose** eyni `.env`-i oxuyur və app konteynerində override edir:

- `DB_URL=jdbc:postgresql://postgres:5432/fleettrack_db`
- `REDIS_HOST=redis`

Beləliklə host adları Compose şəbəkəsinə uyğun olur.

### Əsas `application.properties` parametrləri

| Açar | Default | Məna |
| --- | --- | --- |
| `server.port` | `8080` | HTTP port |
| `server.servlet.context-path` | `/api` | Bütün REST yollarının prefiksi |
| `fleettrack.jwt.expiration-ms` | `86400000` | Access token: 24 saat |
| `fleettrack.rate-limit.capacity` | `100` | Bucket tutumu |
| `fleettrack.rate-limit.refill-tokens` | `100` | Doldurma |
| `fleettrack.rate-limit.refill-duration-minutes` | `1` | Doldurma intervalı |
| `fleettrack.cache.vehicle-summary-ttl` | `300` | Saniyə |
| `fleettrack.cache.driver-summary-ttl` | `300` | Saniyə |
| `fleettrack.scheduler.maintenance-check-cron` | `0 0 6 * * *` | Hər gün 06:00 |
| `fleettrack.scheduler.maintenance-alert-days-ahead` | `7` | Servis xəbərdarlığı pəncərəsi |

Refresh token müddəti kodda sabitdir: **7 gün** (`604800000` ms).

Jackson: naməlum JSON sahələri rədd edilir (`fail-on-unknown-properties=true`), tarixlər timestamp deyil, saat qurşağı UTC.

---

## İşə salma

### 1) Docker Compose (tövsiyə)

Kök qovluqda `.env` hazır olduqdan sonra:

```bash
docker compose up --build
```

Servislər:

| Servis | Konteyner | Port |
| --- | --- | --- |
| Spring Boot | `fleettrack_app` | `8080` |
| PostgreSQL 15 Alpine | `fleettrack_postgres` | `5432` |
| Redis 7 Alpine (`--requirepass`) | `fleettrack_redis` | `6379` |

Volume-lər: `postgres_data`, `redis_data`.

App image-i `Dockerfile` ilə yığılır: Gradle testləri keçmədən (`-x test`) fat JAR, sonra JRE 21 Alpine.

### 2) Lokal JVM + Docker-də yalnız DB/Redis

PostgreSQL və Redis-i Compose ilə və ya lokal quraşdırma ilə işə salın. `.env`-də `DB_URL` və `REDIS_HOST=localhost` saxlayın.

Windows:

```bat
gradlew.bat bootRun
```

Linux / macOS:

```bash
./gradlew bootRun
```

JAR:

```bash
./gradlew clean build
java -jar build/libs/FleetTrack-0.0.1-SNAPSHOT.jar
```

Tətbiq hazır olduqda baza ünvanı:

```
http://localhost:8080/api
```

---

## Swagger / OpenAPI

| Resurs | URL |
| --- | --- |
| Swagger UI | [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html) |
| OpenAPI JSON | [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs) |

UI-da `Authorize` düyməsi ilə `Bearer <accessToken>` əlavə edin (`bearerAuth`). Sessiya saxlanılır (`persist-authorization=true`).

---

## Autentifikasiya

Axın: **login → access JWT + refresh JWT → `Authorization: Bearer <accessToken>`**.

Sessiya yoxdur (`STATELESS`). Filter: `JwtAuthenticationFilter`.

### Seed istifadəçilər (Flyway)

Şifrə hər iki hesab üçün: `secret`

| Username | Rol |
| --- | --- |
| `Samir` | `ADMIN` |
| `Miri` | `FLEET_MANAGER` |

Production-da bu parolları dəyişin və ya seed-i silin.

### Qeydiyyat (yalnız DRIVER)

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "Ali Mammadov",
  "password": "SecurePass123!"
}
```

- Username: 4–50 simvol.
- Şifrə: minimum 6 simvol.
- Rol avtomatik `DRIVER`.
- Cavab: `201` və mətn: `User registered successfully! Please log in.`

Sürücü **profili** ayrıca yaradılır (`POST /api/drivers`). Profilin `firstName` + `lastName` (boşluqla) istifadəçi adıyla eyni olmalıdır (case-insensitive). Məsələn username `Ali Mammadov` → `firstName: Ali`, `lastName: Mammadov`. `userId` həmin hesabın ID-si olmalıdır.

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "Samir",
  "password": "secret"
}
```

Cavab (`AuthResponse`):

```json
{
  "id": 1,
  "username": "Samir",
  "role": "ADMIN",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

Access token claim-ində `role` var. Subject = username.

### Token yeniləmə

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJ..."
}
```

Yeni access + refresh cütü qaytarılır. Etibarsız/bitmiş refresh → xəta.

### Yüksək səviyyəli istifadəçi (yalnız ADMIN)

```http
POST /api/admin/users
Authorization: Bearer <admin-access-token>
Content-Type: application/json

{
  "username": "manager_john",
  "password": "SecurePass123!",
  "role": "FLEET_MANAGER"
}
```

`role` yalnız `ADMIN` və ya `FLEET_MANAGER` ola bilər.

---

## REST API

Bütün qorunan endpoint-lər üçün başlıq:

```http
Authorization: Bearer <accessToken>
```

Səhifələmə cavabı: `PageResponse` (`content`, səhifə metadataları).

### Autentifikasiya

| Metod | Yol | Auth | Təsvir |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | ictimai | DRIVER qeydiyyatı |
| POST | `/api/auth/login` | ictimai | Token cütü |
| POST | `/api/auth/refresh` | ictimai | Token yeniləmə |

### Admin

| Metod | Yol | Rol | Təsvir |
| --- | --- | --- | --- |
| POST | `/api/admin/users` | ADMIN | ADMIN / FLEET_MANAGER yaratmaq |

### Vasitələr — `/api/vehicles`

| Metod | Yol | Rol | Təsvir |
| --- | --- | --- | --- |
| POST | `/` | ADMIN, FLEET_MANAGER | Yeni vasitə |
| GET | `/` | ADMIN, FLEET_MANAGER | Axtarış + səhifələmə |
| GET | `/summaries` | ADMIN, FLEET_MANAGER | Redis keşli xülasə siyahısı |
| GET | `/{id}` | ADMIN, FLEET_MANAGER, DRIVER | Bir vasitə |
| PUT | `/{id}` | ADMIN, FLEET_MANAGER | Yeniləmə |
| DELETE | `/{id}` | ADMIN | Silmə |

**Axtarış query parametrləri:**

| Parametr | Default | Qeyd |
| --- | --- | --- |
| `status` | — | `ACTIVE`, `IN_MAINTENANCE`, `OFFLINE`, `DECOMMISSIONED` |
| `minYear`, `maxYear` | — | İl aralığı |
| `driverId` | — | Təyin olunmuş sürücü |
| `hasAssignedDriver` | — | `true` / `false` |
| `licensePlate` | — | Qismən uyğunluq |
| `page` | `0` | |
| `size` | `20` | |
| `sortBy` | `id` | |
| `direction` | `ASC` | `ASC` / `DESC` |

**Yaratma/yeniləmə body (`VehicleRequest`):**

```json
{
  "make": "Toyota",
  "model": "Prius",
  "year": 2022,
  "licensePlate": "99-AA-999",
  "status": "ACTIVE"
}
```

Nömrə nişanı unikal olmalıdır.

### Sürücülər — `/api/drivers`

| Metod | Yol | Rol | Təsvir |
| --- | --- | --- | --- |
| POST | `/` | ADMIN, FLEET_MANAGER | Profil yaratmaq |
| GET | `/` | ADMIN, FLEET_MANAGER | Səhifələnmiş siyahı (`sort` default `id DESC`) |
| GET | `/summaries` | ADMIN, FLEET_MANAGER | Yalnız `ACTIVE`, keşli |
| GET | `/{id}` | ADMIN, FLEET_MANAGER, DRIVER | DRIVER yalnız öz ID-si |
| PUT | `/{id}` | ADMIN, FLEET_MANAGER, DRIVER | DRIVER yalnız öz profili; `BLOCKED` yenilənmir |
| DELETE | `/{id}` | ADMIN | Soft-delete: `BLOCKED` + vasitə ayrılır |

**Body (`DriverRequest`):**

```json
{
  "firstName": "Ali",
  "lastName": "Mammadov",
  "licenseNumber": "AZ12345678",
  "contactData": {
    "email": "ali@example.com",
    "phone": "+994501234567"
  },
  "assignedVehicleId": 1,
  "userId": 15
}
```

Qaydalar:

- `userId` mövcud `DRIVER` hesabına işarə etməlidir.
- Bir user yalnız bir sürücü profilinə bağlana bilər.
- Ad soyad username ilə üst-üstə düşməlidir.
- Vəsiqə nömrəsi unikal.
- `assignedVehicleId` başqa sürücüdə ola bilməz (1:1).

### Texniki xidmət — `/api/maintenance-logs`

| Metod | Yol | Rol | Təsvir |
| --- | --- | --- | --- |
| POST | `/` | ADMIN, FLEET_MANAGER | Qeyd yaratmaq |
| GET | `/` | ADMIN, FLEET_MANAGER | Səhifələmə (`sortBy` default `scheduledDate DESC`) |
| GET | `/vehicle/{vehicleId}` | ADMIN, FLEET_MANAGER | Vasitəyə görə siyahı |
| GET | `/{id}` | ADMIN, FLEET_MANAGER | Bir qeyd |
| PUT | `/{id}` | ADMIN, FLEET_MANAGER | Yeniləmə |
| DELETE | `/{id}` | ADMIN | Silmə |

```json
{
  "vehicleId": 5,
  "description": "Oil change and tire rotation",
  "scheduledDate": "2026-09-15",
  "completed": false
}
```

### GPS — `/api/vehicle-locations`

| Metod | Yol | Rol | Təsvir |
| --- | --- | --- | --- |
| POST | `/` | ADMIN, FLEET_MANAGER, DRIVER | Koordinat yenilə + WebSocket yayım |
| POST | `/{vehicleId}/offline` | ADMIN, FLEET_MANAGER, DRIVER | Status `OFFLINE` + Redis xəbərdarlığı |

```json
{
  "vehicleId": 5,
  "latitude": 40.409264,
  "longitude": 49.867092
}
```

Enlem: `[-90, 90]`, uzunluq: `[-180, 180]`. Sürücü yalnız özünə təyin olunmuş vasitəni yeniləyə bilər.

### Hesabatlar — `/api/reports`

| Metod | Yol | Rol | Təsvir |
| --- | --- | --- | --- |
| GET | `/fleet-status/pdf` | ADMIN, FLEET_MANAGER | `fleet-status-report.pdf` yükləmə |

Cavab: `application/pdf`, `Content-Disposition: attachment`. Məzmun: ümumi park sayı, status üzrə saylar, vasitə cədvəli, texniki xidmət qeydləri.

---

## WebSocket (GPS və xəbərdarlıqlar)

Konfiqurasiya: `WebSocketConfig`.

| Parametr | Dəyər |
| --- | --- |
| Handshake (SockJS) | `/api/ws/vehicles` |
| Application prefix | `/app` |
| Broker | `/topic` (sadə in-memory broker) |
| GPS göndərmə | STOMP destination: `/app/gps` |
| GPS abunə | `/topic/vehicle-locations` |
| Xəbərdarlıq abunə | `/topic/fleet-alerts` |

Handshake Spring Security-də `/ws/**` olaraq açıqdır. Frontend SockJS client ilə bağlanır, sonra STOMP `SUBSCRIBE` edir.

GPS payload `VehicleLocationRequest` ilə eynidir. Server eyni `VehicleLocationService.updateLocation` metodunu çağırır (persist + broadcast).

**GPS yayım nümunəsi:**

```json
{
  "vehicleId": 5,
  "licensePlate": "99-AA-999",
  "latitude": 40.409264,
  "longitude": 49.867092,
  "timestamp": "2026-08-31T16:00:00Z"
}
```

**Xəbərdarlıq növləri (`FleetAlertMessage.type`):**

| type | Nə vaxt |
| --- | --- |
| `VEHICLE_OFFLINE` | `POST .../offline` |
| `MAINTENANCE_DUE` | Plan tarixi bu gün və ya növbəti 7 gün |
| `MAINTENANCE_OVERDUE` | Plan tarixi keçib, tamamlanmayıb |

---

## Redis: keş və xəbərdarlıqlar

İki istifadə:

1. **Cache** (`CacheConfig`, profil `!test`)
   - `vehicleSummaries` — `GET /api/vehicles/summaries`
   - `driverSummaries` — `GET /api/drivers/summaries` (yalnız ACTIVE)
   - TTL default 300 saniyə
   - Create/update/delete zamanı `@CacheEvict(allEntries = true)`

2. **Pub/Sub**
   - Kanal adı: `fleet-alerts`
   - Publisher: `FleetAlertPublisher`
   - Subscriber: `FleetAlertSubscriber` → WebSocket

Lettuce pool: max-active 16, timeout 60 saniyə.

---

## Planlaşdırıcılar (schedulers)

`@EnableScheduling` tətbiq səviyyəsində aktiv olmalıdır (Spring Boot default + scheduler komponentləri).

### Texniki xidmət yoxlaması — `MaintenanceScheduler`

- Profil: `!test` (test zamanı işləmir)
- Cron: `fleettrack.scheduler.maintenance-check-cron` (default hər gün 06:00)
- Tamamlanmamış və `scheduled_date <= today + 7 gün` olan qeydlər oxunur
- Keçmiş tarix → `MAINTENANCE_OVERDUE`, əks halda `MAINTENANCE_DUE`

### Sürücü təmizliyi — `DriverCleanupScheduler`

- Cron: hər gün 02:00 (`0 0 2 * * ?`)
- `BLOCKED` və `updated_at` 30 gündən köhnə olan sürücülər **hard delete**

---

## Rate limiting

`RateLimitInterceptor` Swagger və WebSocket xaricində bütün HTTP yollarına düşür.

- Açarsız (anonim): `ip:<remoteAddr>`
- Login olmuş: `user:<username>`
- Default: **100 sorğu / 1 dəqiqə** (greedy refill)
- Limit aşımı: HTTP **429** (`RateLimitExceededException`)

Test profilində limit 1000-ə qaldırılıb.

---

## Xəta formatı

`GlobalExceptionHandler` vahid JSON qaytarır (`ErrorResponse`):

```json
{
  "timestamp": "2026-08-31T16:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/vehicles",
  "validationErrors": {
    "licensePlate": "License plate is required"
  }
}
```

| HTTP | Tipik səbəb |
| --- | --- |
| 400 | Validasiya, biznes qaydası, səhv JSON |
| 401 | Yanlış şifrə, etibarsız JWT |
| 403 | Rol və ya öz resursu deyil |
| 404 | Entity tapılmadı |
| 409 | Unikal məhdudiyyət (məs. username, nömrə nişanı) |
| 405 | Yanlış HTTP metodu |
| 415 | Content-Type dəstəklənmir |
| 429 | Rate limit |
| 500 | Gözlənilməyən xəta |

401/403 üçün REST entry point və access denied handler JSON cavab verir (HTML login səhifəsi yoxdur).

CORS: bütün origin pattern-lər, credentials aktiv, `Authorization` və `Content-Disposition` expose olunur.

---

## Testlər

```bash
./gradlew test
```

Test mühiti (`src/test/resources/application-test.properties`):

- In-memory **H2** (PostgreSQL compatibility mode)
- Flyway və Redis auto-config **söndürülür**
- Sadə in-memory keş
- `TestRedisConfig` / `TestCacheConfig` stub-ları
- JWT test açarı
- `MaintenanceScheduler` `!test` profili ilə iştirak etmir

Əsas smoke test: `FleetTrackApplicationTests`.

---

## Təhlükəsizlik qeydləri

1. `.env` və JWT secret-i versiya nəzarətinə salmayın. Production-da `JWT_SECRET` kifayət qədər uzun HMAC açarı olmalıdır.
2. Seed istifadəçilərin `secret` parolu yalnız inkişaf üçündür.
3. WebSocket handshake hazırda JWT tələb etmir (`/ws/**` permitAll). Canlı xəritəni internetə açıq qoymadan əvvəl STOMP handshake-ə token yoxlaması əlavə etmək lazımdır.
4. CORS `allowedOriginPatterns: *` + `allowCredentials: true` inkişaf üçündür; production-da konkret frontend origin yazın.
5. Rate limit proses yaddaşına bağlıdır (instansiya başına). Bir neçə replica üçün Redis-backed bucket nəzərdən keçirin.
6. HikariCP: max pool 20, min idle 5.

---

## Lisenziya

OpenAPI təsvirində Apache 2.0 qeyd olunub. Layihə üçün ayrıca lisenziya faylı yoxdursa, komanda daxilində razılaşmanı təsdiqləyin.

---

## Əlaqə (OpenAPI)

- Ad: FleetTrack Team  
- E-poçt: support@fleettrack.com  

Swagger UI-dakı contact məlumatı `OpenApiConfig` içindədir.
#   F l e e t T r a c k  
 