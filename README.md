# MobilityShield — Mobile Device Management & Cybersecurity Monitoring System

MobilityShield is an enterprise Mobile Device Management (MDM) and centralized Cybersecurity Monitoring backend built with Java 27, Spring Boot 4, Firebase Authentication, Cloud Firestore, and Firebase Cloud Messaging (FCM).

---

## 1. Project Overview

MobilityShield provides centralized security oversight and device administration for enterprise mobile fleets:
- **Device Intelligence:** Mobile enrollment, hardware telemetry (battery, RAM, storage, network), heartbeat tracking, and health status evaluation.
- **Cybersecurity & Threat Detection:** Real-time event monitoring (root detection, disabled screen locks, outdated OS levels, suspicious activity), algorithmic security scoring (0–100), automated security alerts, and a **3-Attempt Failed Login Quarantine Rule**.
- **Cloud Device Management:** Role-based administrative dashboard, user inventory, device inventory, remote quarantine, recovery justification, compliance policy enforcement, and audit logging.

---

## 2. Architecture

MobilityShield employs a **Modular Monolith** architecture:
- **Authentication & Security:** Stateless Spring Security filter verifying Firebase Bearer ID tokens.
- **Data Layer:** Google Cloud Firestore as the single authoritative database (no JPA, Hibernate, or SQL dependencies).
- **Communication Layer:** Firebase Cloud Messaging (FCM) for push notifications to mobile devices and administrators.
- **Observability:** Spring Boot Actuator for health checks and structured audit logs.

```
+-----------------------------------------------------------------------------------+
|                            MobilityShield API                                     |
|                                                                                   |
|  +--------------------+   +---------------------------+   +--------------------+  |
|  | Device             |   | Cybersecurity & Threat    |   | Cloud Device       |  |
|  | Intelligence       |   | Detection Engine          |   | Management (Admin) |  |
|  | (Enroll, Telemetry,|   | (Score, Events, Alerts,   |   | (Quarantine,       |  |
|  |  Heartbeat, Health)|   |  3-Attempt Quarantine)    |   |  Recovery, Audits) |  |
|  +--------------------+   +---------------------------+   +--------------------+  |
|            |                           |                            |             |
|            +---------------------------+----------------------------+             |
|                                        |                                          |
|                          Spring Security Stateless Layer                          |
|                     (Firebase ID Token Verification & RBAC)                       |
+-----------------------------------------------------------------------------------+
                                         |
                         +-------------------------------+
                         |     Firebase Admin SDK        |
                         |   - Authentication            |
                         |   - Cloud Firestore           |
                         |   - Cloud Messaging (FCM)     |
                         +-------------------------------+
```

---

## 3. Technology Stack

- **Language:** Java 27 (Oracle JDK 27.0.1.1)
- **Framework:** Spring Boot 4.1.2 (Web, Security, Validation, Actuator)
- **Database:** Google Cloud Firestore (`com.google.firebase:firebase-admin:9.8.0`)
- **Authentication:** Firebase Authentication (Bearer ID Tokens)
- **Notifications:** Firebase Cloud Messaging (FCM)
- **Build Tool:** Apache Maven 3.9.9 (via Maven Wrapper `mvnw.cmd` / `mvnw`)
- **Testing:** JUnit 5, Mockito

---

## 4. Project Structure

```text
MobilityShield/
├── backend/
│   ├── src/main/java/com/mobilityshield/api/
│   │   ├── config/
│   │   │   ├── FirebaseConfig.java
│   │   │   ├── FirestoreConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── CorsConfig.java
│   │   ├── common/
│   │   │   ├── ApiResponse.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── Constants.java
│   │   ├── security/
│   │   │   ├── Role.java
│   │   │   ├── AuthenticatedUser.java
│   │   │   ├── CustomUserDetails.java
│   │   │   ├── FirebaseTokenService.java
│   │   │   ├── FirebaseAuthenticationFilter.java
│   │   │   └── SecurityContextService.java
│   │   ├── auth/
│   │   │   ├── AuthController.java
│   │   │   ├── AuthService.java
│   │   │   └── dto/RegisterProfileRequest.java
│   │   ├── user/
│   │   │   ├── User.java
│   │   │   ├── UserRepository.java
│   │   │   ├── UserService.java
│   │   │   ├── UserController.java
│   │   │   └── dto/
│   │   ├── device/
│   │   │   ├── Device.java
│   │   │   ├── DeviceRepository.java
│   │   │   ├── DeviceService.java
│   │   │   ├── DeviceController.java
│   │   │   └── dto/
│   │   ├── telemetry/
│   │   │   ├── Telemetry.java
│   │   │   ├── TelemetryRepository.java
│   │   │   ├── TelemetryService.java
│   │   │   ├── TelemetryController.java
│   │   │   └── dto/
│   │   ├── cybersecurity/
│   │   │   ├── SecurityEvent.java & Repository
│   │   │   ├── SecurityAlert.java & Repository
│   │   │   ├── LoginAttempt.java & Repository
│   │   │   ├── SecurityScoreService.java
│   │   │   ├── ThreatDetectionService.java
│   │   │   ├── LoginAttemptService.java
│   │   │   ├── SecurityService.java
│   │   │   ├── SecurityController.java
│   │   │   └── dto/
│   │   ├── admin/
│   │   │   ├── AdminService.java
│   │   │   ├── AdminController.java
│   │   │   └── dto/
│   │   ├── policy/
│   │   │   ├── DevicePolicy.java & Repository
│   │   │   ├── PolicyService.java
│   │   │   └── dto/
│   │   ├── notification/
│   │   │   ├── FcmService.java
│   │   │   └── NotificationService.java
│   │   ├── audit/
│   │   │   ├── AuditLog.java & Repository
│   │   │   └── AuditService.java
│   │   ├── exception/
│   │   └── MobilityshieldApiApplication.java
│   └── src/main/resources/
│       └── application.yml
├── postman/
│   └── MobilityShield_API_Collection.json
├── Dockerfile
├── render.yaml
└── README.md
```

---

## 5. Firebase Setup

- **Project ID:** `mobilityshield-e60b3`
- **Region:** `asia-south1`
- **Service Account Credentials:**
  Downloaded from Firebase Console (`Project Settings` > `Service Accounts` > `Generate New Private Key`) and stored securely outside the source tree:
  ```text
  C:\MobilityShield-Secrets\firebase-service-account.json
  ```

---

## 6. Firestore Database Design

Cloud Firestore is the sole database engine. Collections:

| Collection | Document ID | Purpose |
|---|---|---|
| `users` | Firebase UID | User identity, email, assigned role (`USER` or `ADMIN`), status, and FCM token. |
| `devices` | `dev-<uuid>` | Enrolled mobile hardware specs, status (`ACTIVE`, `INACTIVE`, `QUARANTINED`), security score, quarantine reason. |
| `telemetry` | `tel-<uuid>` | Historical device telemetry records (battery, RAM, storage, Wi-Fi, network type, timestamp). |
| `security_events`| `evt-<uuid>` | Ingested cybersecurity events (`ROOT_DETECTED`, `SCREEN_LOCK_DISABLED`, `POLICY_VIOLATION`, etc.). |
| `security_alerts`| `alt-<uuid>` | Security incident alerts categorized by severity (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`). |
| `login_attempts` | `att-<uuid>` | Auditable authentication attempts with success flags and timestamps. |
| `device_policies`| `pol-<uuid>` | Enterprise compliance requirements (min OS, screen lock, root blocking, max failed attempts). |
| `audit_logs` | `log-<uuid>` | Immutable audit records of all administrative actions (actor UID, target ID, action type, metadata). |

---

## 7. Firebase Authentication & Role-Based Authorization

1. Mobile clients (Flutter) authenticate using Firebase Authentication and obtain an ID Token (JWT).
2. The client attaches the token in the HTTP Authorization header:
   ```http
   Authorization: Bearer <FIREBASE_ID_TOKEN>
   ```
3. `FirebaseAuthenticationFilter` intercepts the request, verifies the token against Google Firebase servers via `FirebaseAuth.verifyIdToken()`, extracts the verified `uid` and `email`, and loads the authoritative role from Firestore.
4. **Role Isolation:**
   - Default role for all newly registered users is strictly `USER`.
   - Normal users are prevented from escalating their role to `ADMIN`.
   - `/api/v1/admin/**` endpoints are strictly protected by `@PreAuthorize("hasRole('ADMIN')")`.
   - Resource access strictly verifies ownership (`securityContextService.assertOwnershipOrAdmin()`), preventing users from accessing or modifying other users' devices.

---

## 8. Environment Variable Configuration (Windows PowerShell)

Set environment variables in your PowerShell session:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\MobilityShield-Secrets\firebase-service-account.json"
$env:FIREBASE_PROJECT_ID = "mobilityshield-e60b3"
$env:FIREBASE_DATABASE_ID = "default"
$env:PORT = "8080"
```

To set permanently on Windows:
```powershell
[System.Environment]::SetEnvironmentVariable('GOOGLE_APPLICATION_CREDENTIALS', 'C:\MobilityShield-Secrets\firebase-service-account.json', 'User')
```

---

## 9. How to Run Locally

From the `backend` directory, start the Spring Boot application using the Maven wrapper:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The server binds to port `8080` (or `http://localhost:8080`).

---

## 10. How to Test Firebase Connection

Run the integrated Firebase verification test, which executes live write, read, and cleanup against Cloud Firestore:

```powershell
cd backend
.\mvnw.cmd test -Dtest=FirebaseConnectionIntegrationTest
```

To execute the entire automated unit and security test suite:
```powershell
cd backend
.\mvnw.cmd test
```

---

## 11. API Documentation

Base Path: `/api/v1`

### Health & Observability (Public)
- `GET /actuator/health` — Application health check
- `GET /actuator/info` — Application info

### Authentication & User Profile
- `POST /api/v1/auth/register-profile` — Binds name and FCM token to authenticated Firebase user
- `GET /api/v1/users/me` — Retrieves current user profile
- `PUT /api/v1/users/me` — Updates current user profile

### Device Intelligence
- `POST /api/v1/devices/enroll` — Enrolls a mobile device (generates authoritative `dev-<uuid>`)
- `GET /api/v1/devices` — Lists enrolled devices (user sees own devices; admin sees all)
- `GET /api/v1/devices/{deviceId}` — Retrieves device details
- `DELETE /api/v1/devices/{deviceId}` — Deregisters device
- `POST /api/v1/devices/{deviceId}/heartbeat` — Records heartbeat (rejected if quarantined)
- `GET /api/v1/devices/{deviceId}/health` — Evaluates device battery, RAM, storage, and offline status
- `POST /api/v1/telemetry` — Submits hardware telemetry snapshot
- `GET /api/v1/telemetry/{deviceId}` — Retrieves telemetry history
- `GET /api/v1/telemetry/{deviceId}/latest` — Retrieves latest telemetry record

### Cybersecurity & Threat Detection
- `GET /api/v1/security/score/{deviceId}` — Calculates dynamic security score (0–100)
- `POST /api/v1/security/events` — Ingests security event (root detection, lock status, anomaly)
- `GET /api/v1/security/events` — Lists security events
- `GET /api/v1/security/alerts` — Lists security alerts
- `GET /api/v1/security/alerts/{alertId}` — Retrieves specific security alert
- `PATCH /api/v1/security/alerts/{alertId}/read` — Marks security alert as read/acknowledged
- `GET /api/v1/security/compliance/{deviceId}` — Evaluates policy compliance status
- `POST /api/v1/security/login-attempts` — Ingests application authentication attempts

### Cloud Device Management (Admin Only)
- `GET /api/v1/admin/dashboard` — Aggregates fleet statistics, alert counts, and risk distribution
- `GET /api/v1/admin/users` — Lists all registered users
- `GET /api/v1/admin/devices` — Lists all fleet devices
- `GET /api/v1/admin/alerts` — Lists all security alerts
- `PATCH /api/v1/admin/users/{uid}/status` — Updates user status (`ACTIVE`, `SUSPENDED`)
- `POST /api/v1/admin/devices/{deviceId}/quarantine` — Places device into quarantine
- `POST /api/v1/admin/devices/{deviceId}/recover` — Lifts quarantine with justification
- `PATCH /api/v1/admin/devices/{deviceId}/status` — Updates device state
- `GET /api/v1/admin/audit-logs` — Retrieves immutable administrative audit trail
- `GET /api/v1/admin/policies` — Lists compliance policies
- `PUT /api/v1/admin/policies` — Updates active compliance policy

---

## 12. Security Score Engine

- **Score Range:** 0 to 100 (Initial baseline: 100).
- **Point Deductions:**
  - Root binary detected: `-30`
  - Screen lock disabled: `-20`
  - Unsupported Android OS version: `-15`
  - Suspicious activity reported: `-20`
  - Active quarantine state: `-15`
- **Deduplication:** Repeated events for the same condition do not produce duplicate deductions.
- **Risk Tiers:**
  - `80 – 100`: **Low Risk**
  - `60 – 79`: **Medium Risk**
  - `40 – 59`: **High Risk**
  - `0 – 39`: **Critical Risk**

---

## 13. Three-Attempt Quarantine Rule & Technical Limitations

### Implementation
- The backend tracks consecutive failed authentication attempts in the `login_attempts` collection.
- Upon 3 consecutive failed attempts on a device:
  1. The system transitions the device status to `QUARANTINED`.
  2. Generates a `CRITICAL` security alert.
  3. Records an audit log and security event.
  4. Dispatches push notifications to administrators and the user.
  5. Blocks protected enterprise operations (heartbeats, telemetry) on that device.
  6. Requires an authorized administrator to review and call the recovery endpoint.

### Technical Limitations & Trust Boundary
1. **Application Authentication vs. Physical Lock Screen:** A standard Android application sandbox cannot intercept or count physical lock-screen PIN/Pattern attempts. The 3-attempt quarantine rule applies strictly to **MobilityShield application authentication attempts**.
2. **Server-Side Enforcement:** Client-reported attempt counters are not trusted. Attempt records are tracked and evaluated server-side.
3. **Quarantine Scope:** Server-side quarantine blocks access to protected enterprise backend resources and APIs. It does not remotely disable the user's physical smartphone hardware or network antennas.

---

## 14. Admin Provisioning Instructions

To provision an administrative account:
1. Have the administrator sign up via Firebase Authentication (email/password).
2. Retrieve the user's Firebase UID from the Firebase Console.
3. In the Cloud Firestore `users` collection:
   - Find or create the document with Document ID = `<UID>`.
   - Set the `role` field to `ADMIN` (string).
   - Set `status` to `ACTIVE`.
4. The user's subsequent requests will carry `ROLE_ADMIN` authorization automatically.

---

## 15. Testing with Postman

1. Open Postman and import [MobilityShield_API_Collection.json](file:///c:/Users/nares/OneDrive/Tài%20liệu/MobilitySheild/postman/MobilityShield_API_Collection.json).
2. Configure Collection Variables:
   - `baseUrl`: `http://localhost:8080` (or Render deployed URL)
   - `userToken`: Bearer ID token of a standard user.
   - `adminToken`: Bearer ID token of an admin user.
   - `deviceId`: Enrolled device identifier.
3. Run the requests in order:
   - Verify `Public Health Check`.
   - Call `Register User Profile`.
   - Call `Enroll Mobile Device`.
   - Submit telemetry and retrieve health status.
   - Query cybersecurity score and compliance.
   - Test admin operations using `adminToken`.

---

## 16. Building and Packaging

Build the executable JAR package:

```powershell
cd backend
.\mvnw.cmd clean package
```

The packaged JAR file is located at:
`backend/target/mobilityshield-api-0.0.1-SNAPSHOT.jar`

Run the JAR directly:
```powershell
java -jar backend/target/mobilityshield-api-0.0.1-SNAPSHOT.jar
```
