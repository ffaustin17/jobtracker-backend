# JobTracker - Backend Api

The backend service powering [JobTrackr](https://github.com//ffaustin17/jobtrackr-frontend), a full stack job application
tracker designed to help users stay organized throughout their job search.

Built with **Spring Boot**, **Spring Data JPA**, **Spring Security**. **Java Mail-Sender**, and **PostgreSQL**, this backend provides
secure user management and full CRUD  support for job applications.

---

## Features

- JWT-based authentication (login, registration, logout)
- Email verification system
- Password reset via email
- Full CRUD for job applications
- Job filtering, searching, and sorting
- Pagination support
- Email sending via Gmail SMTP (or future possible SendGrid integration)

---

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL (or H2 for devs)
- JWT (Json Web Tokens)
- Java Mail Sender
- Maven

---

## Setup Instructions

### 1. Clone the repo
```bash
git clone https://github.com//ffaustin17/jobtrackr-backend.git
cd jobtrackr-backend
```

### 2. Configure Environment Variables

Create `application.properties` (or use `application-local.properties` with:
<pre>
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/jobtrackr_db
spring.datasource.username=your-db-user
spring.datasource.password=your-db-password
spring.jpa.hibernate.ddl-auto=update

# JWT Secret
jwt.secret=your-secret-key

# Email (Gmail SMTP example)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.protocol=smtp
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

</pre>
> Do not commit credential -- use environment variables or Spring profiles.


### 3. Run the App
```bash
./mvnw spring-boot:run
```
Or from your IDE of choice.

---

## API Endpoints Summary

### Public Routes

| Endpoint                    | Method | Description            |
|-----------------------------|--------|------------------------|
| `/api/auth/register`        | POST   | Register new user      |
| `/api/auth/login`           | POST   | Login & get JWT        |
| `/api/auth/verify`          | GET    | Verify email via token |
| `/api/auth/forgot-password` | POST   | Request password reset |
| `/api/auth/reset-password`  | POST   | Submit new password    |


### Protected Routes (JWT required)

| Endpoint             | Method     | Description                          |
|----------------------|------------|--------------------------------------|
| `/api/user/me`       | GET        | Get current user info                |
| `/api/user/password` | PUT        | Update user password                 |
| `/api/jobs`          | GET/POST   | List/Create job apps                 |
| `/api/jobs/{id}`     | PUT/DELETE | Update/Delete                        |
| `/api/jobs/filter`   | POST       | Filter jobs by status, company, etc. |


---

## Email Features

- Emails are sent for:
    - Account verification (enabling new account)
    - Password reset
- Sent via Gmail SMTP (replace with your mail service of choice)


---

