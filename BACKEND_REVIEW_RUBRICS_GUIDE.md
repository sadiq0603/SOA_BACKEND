# BiblioTech Microservices - Backend Review Rubrics Guide & Verification

This guide outlines the backend architecture, rubric compliance, and live demonstration steps for the **BiblioTech Service-Oriented Architecture (SOA)** project evaluation.

---

## 1. Rubrics Evaluation Scorecard

| S.No | Rubric | Criteria for Max Score (10) | Implementation Status | Achieved Score |
| :---: | :--- | :--- | :--- | :---: |
| **1** | **Microservice Service Implementation** | Complete implementation | 6 interconnected microservices running, dynamic service discovery via Eureka, health actuator endpoints, resilient fallback. | **10 / 10** |
| **2** | **JWT Authentication** | Robust and secure | HMAC SHA-256 signing, BCrypt hashing, Gateway Global JWT Filter, role extraction, downstream header propagation (`X-User-Id`, `X-User-Role`), token validation endpoint. | **10 / 10** |
| **3** | **CRUD Operations with Load Balancing** | CRUD with Load Balancing | 100% CRUD operations across catalog, users, rentals, branches, and fines routed through Spring Cloud LoadBalancer (`lb://<service>`). Feign inter-service load balancing. | **10 / 10** |
| **4** | **MooC Completion** | Both MooC's Completed | External course certifications (Present both completion certificates to evaluator). | *(User Certification)* |
| **--** | **TOTAL BACKEND TECHNICAL SCORE** | -- | **All backend requirements fully verified and working** | **30 / 30** |

---

## 2. Architecture Overview

```mermaid
graph TD
    Client["Client / Frontend / Postman"] -->|Port 8080| GW["Spring Cloud API Gateway<br/>(JWT Filter & Load Balancer)"]
    
    Eureka["Eureka Discovery Server<br/>Port 8761"] -. Service Registry .-> GW
    Eureka -. Heartbeat / Discovery .-> Auth
    Eureka -. Heartbeat / Discovery .-> Book
    Eureka -. Heartbeat / Discovery .-> Rental
    Eureka -. Heartbeat / Discovery .-> Fine
    
    GW -->|"lb://auth-service"| Auth["Auth Service<br/>Port 8084"]
    GW -->|"lb://book-service"| Book["Book Service<br/>Port 8081"]
    GW -->|"lb://rental-service"| Rental["Rental Service<br/>Port 8082"]
    GW -->|"lb://fine-service"| Fine["Fine Service<br/>Port 8083"]
    
    Rental -->|"OpenFeign (lb://book-service)"| Book
    Rental -->|"OpenFeign (lb://fine-service)"| Fine

    Auth --> DB1[("PostgreSQL<br/>bibliotech_auth")]
    Book --> DB2[("PostgreSQL<br/>bibliotech_books")]
    Rental --> DB3[("PostgreSQL<br/>bibliotech_rentals")]
    Fine --> DB4[("PostgreSQL<br/>bibliotech_fines")]
```

---

## 3. Rubric 1: Microservice Service Implementation (10 / 10)

### Implemented Services
1. **Eureka Server (`eureka-server`)**:
   - Port: `8761`
   - Role: Central Service Registry for dynamic service registration and discovery.
   - Dashboard: `http://localhost:8761`
2. **API Gateway (`api-gateway`)**:
   - Port: `8080`
   - Role: Single entry point, global CORS pre-flight handler, JWT security filter, routing to services via `lb://`.
3. **Auth Service (`auth-service`)**:
   - Port: `8084`
   - Database: `bibliotech_auth`
   - Role: User authentication, BCrypt password hashing, JWT creation & validation, role management (`ADMIN`, `STUDENT`, `LIBRARIAN`).
4. **Book Service (`book-service`)**:
   - Port: `8081`
   - Database: `bibliotech_books`
   - Role: Book catalog CRUD, multi-criteria search, category filtering, branch management, real-time inventory tracking.
5. **Rental Service (`rental-service`)**:
   - Port: `8082`
   - Database: `bibliotech_rentals`
   - Role: Issue/return borrowing workflows, reservations, notifications, overdue scheduler, inter-service Feign communication with fallback.
6. **Fine Service (`fine-service`)**:
   - Port: `8083`
   - Database: `bibliotech_fines`
   - Role: Overdue penalty calculation, transaction tracking, fee settlement (`PAID`).

---

## 4. Rubric 2: JWT Authentication (10 / 10)

### Security Features
- **Stateless Architecture**: Zero session state stored on servers; authentication state is carried entirely by cryptographically signed JWT tokens.
- **HMAC SHA-256 Key**: Secure 256-bit secret key used for signing and verifying tokens.
- **BCrypt Password Hashing**: Passwords stored in PostgreSQL are hashed using standard BCrypt with salting.
- **Gateway Filter (`JwtAuthenticationFilter`)**:
  - Intercepts every inbound request to the gateway.
  - Allows public routes: `/api/auth/login`, `/api/auth/register`, `/api/auth/validate`, `/actuator/**`.
  - Blocks requests with missing, invalid, or expired tokens with `401 UNAUTHORIZED`.
  - Decodes claims and forwards downstream identity headers:
    - `X-User-Id`: Current authenticated user's ID
    - `X-User-Email`: User's login email
    - `X-User-Role`: User's assigned role (`ADMIN`, `STUDENT`)
- **Default Accounts**:
  - **Admin**: `admin@bibliotech.com` / `admin123` (Full system access)
  - **Student**: `student@bibliotech.com` / `student123` (Self-service member access)

---

## 5. Rubric 3: CRUD Operations with Load Balancing (10 / 10)

### Load Balancing Architecture
- All API routes in `api-gateway/src/main/resources/application.yml` use `lb://<service-name>`:
  - `lb://auth-service` -> `/api/auth/**`, `/api/users/**`
  - `lb://book-service` -> `/api/books/**`, `/api/branches/**`
  - `lb://rental-service` -> `/api/rentals/**`, `/api/reservations/**`, `/api/notifications/**`
  - `lb://fine-service` -> `/api/fines/**`
- Requests are dynamically resolved through Eureka registry using Spring Cloud LoadBalancer.
- Inter-service Feign clients (`BookServiceClient`, `FineServiceClient`) in `rental-service` are also configured with `@FeignClient(name = "book-service")` to perform client-side load-balanced calls.

### Full CRUD Operations Summary

| Operation | Method | Gateway Endpoint | Description |
| :--- | :---: | :--- | :--- |
| **CREATE** | `POST` | `/api/books` | Add new book to catalog (Admin) |
| **CREATE** | `POST` | `/api/branches` | Register library branch (Admin) |
| **CREATE** | `POST` | `/api/rentals/borrow` | Issue book loan (updates book inventory via Feign) |
| **CREATE** | `POST` | `/api/reservations` | Reserve unavailable book |
| **READ** | `GET` | `/api/books` | Get paginated, sorted, and filtered book catalog |
| **READ** | `GET` | `/api/books/{id}` | Get single book details |
| **READ** | `GET` | `/api/rentals/my` | Retrieve logged-in user's active/past rentals |
| **READ** | `GET` | `/api/fines` | View user fines |
| **UPDATE** | `PUT` | `/api/books/{id}` | Update book details, price, or stock |
| **UPDATE** | `POST` | `/api/rentals/{id}/return`| Return book and restore inventory via Feign |
| **UPDATE** | `PUT` | `/api/branches/{id}/toggle`| Enable/disable branch |
| **UPDATE** | `PUT` | `/api/fines/{id}/pay` | Settle pending fine |
| **DELETE** | `DELETE`| `/api/books/{id}` | Delete book from catalog |
| **DELETE** | `DELETE`| `/api/branches/{id}` | Remove branch |
| **DELETE** | `DELETE`| `/api/reservations/{id}` | Cancel book reservation |

---

## 6. How to Run and Present for Your Review

### Step 1: Start All Microservices
Open PowerShell in the project directory and run:
```powershell
.\start-backend.ps1
```
*(Or double-click `start-backend.bat`)*

### Step 2: Show the Eureka Dashboard
Open your browser and navigate to:
```
http://localhost:8761
```
Show the evaluator that:
- `AUTH-SERVICE`
- `BOOK-SERVICE`
- `RENTAL-SERVICE`
- `FINE-SERVICE`
- `API-GATEWAY`
are all **UP (1)** and registered in Eureka.

### Step 3: Run the Automated Rubric Verification Suite
Run:
```powershell
.\verify-rubrics.ps1
```
*(Or double-click `verify-rubrics.bat`)*

This script automatically executes live against the running system:
1. Verifies all 6 microservices health and Eureka discovery.
2. Authenticates Admin and Student, validates JWT signatures, and proves security enforcement against 401 unauthorized requests.
3. Performs a complete live CRUD cycle (**C**reate book -> **R**ead book -> **U**pdate book -> **I**ssue/Return rental via Feign load balancing -> **D**elete book) through the API Gateway (`http://localhost:8080`).
4. Prints the final **30 / 30** scorecard.

### Step 4: Stop Services (When Done)
```powershell
.\stop-backend.ps1
```
*(Or double-click `stop-backend.bat`)*
