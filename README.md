
---

# 🐇 Bunnyland API

**Bunnyland** is a community platform for bunny owners 🐰 — a place to connect, share care tips, and help each other with bunny-sitting.
Built with **Java 21 + Spring Boot 3**.

---

## ✨ Features (planned)

### MVP

* **User accounts**

    * Register/login as a bunny owner
    * Admin role for content & moderation

```
{
  "email": "bunnylover@example.com",
  "password": "fluffy123",
  "displayName": "Alice BunnyMom",
  "city": "Hamburg",
  "country": "Germany"
}

```

```
{
  "email": "bunnylover@example.com",
  "password": "fluffy123"
}
{
  "email": "aolani@example.com",
  "password": "fluffy123"
}
```

* **Bunny profiles**

    * Each user can register their bunny (name, breed, age, photos, notes)

* **Friendships**

    * Find other bunny owners by city/breed
    * Send and accept friend requests

* **Announcements**

    * “I am traveling and need someone to care for my bunny” posts
    * Filter by location & dates

```
{
  "title": "Traveling to Spain — need bunny host",
  "description": "I will be away 12–20 Oct. My bunny is calm, vaccinated, loves hay and coriander.",
  "city": "Hamburg",
  "country": "Germany",
  "startDate": "2025-10-12",
  "endDate": "2025-10-20"
}
```

* **Articles (admin only)**

    * Bunny care, food, health advice
    * Read-only for users, managed by admin

* **Info requests**

    * Request specific info about a bunny → admin approval required

### Future Ideas

* 🗨️ Direct chat between owners
* 🔔 Notifications & reminders
* 🐰 Bunny-sitting calendar & availability slots
* ⭐ Trust badges, ratings, and reviews
* 🌍 Multi-language support
* 📱 Mobile app frontend

---

## 🛠️ Tech Stack

* **Java 21**
* **Spring Boot 3** (Web, Security, Data JPA, Validation)
* **PostgreSQL** (Dockerized)
* **Flyway** (DB migrations)
* **Springdoc OpenAPI** (Swagger UI docs)
* **JWT** authentication
* **Docker Compose** (for easy local setup)

---

## 📂 Project Structure

```
src/main/java/fur/bunnyland/bunnylandapi
  ├── api/          # REST controllers
  ├── domain/       # Entities (User, Bunny, Announcement, etc.)
  ├── repository/   # Spring Data JPA repositories
  ├── service/      # Business logic
  └── config/       # Security & app configuration
```

---

## 🐇 Database Schema (MVP)

* **User**: id, email, password, displayName, city, country, roles
* **Bunny**: id, ownerId, name, breed, age, notes, photos
* **Friendship**: requesterId, addresseeId, status
* **Announcement**: ownerId, type, description, city, dates, status
* **Article**: id, adminId, title, content, tags
* **BunnyInfoRequest**: bunnyId, requesterId, status

---

## 🚀 Running Locally

### 1. Start Postgres with Docker

```bash
docker compose up -d
```

### 2. Run the app

```bash
./mvnw spring-boot:run
```

App will be available at:
👉 `http://localhost:8080`

### 3. Swagger UI

👉 `http://localhost:8080/swagger-ui.html`

---

## 🤝 Contributing

1. Fork this repo
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m "Add amazing feature"`
4. Push: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📜 License

MIT License — feel free to use, share, and improve 🐰

---

---

## 👩‍💻 Author

## 👩‍💻 Author

**Kateryna Yashnyk**  
[GitHub: lunaridelmar](https://github.com/lunaridelmar) · [CV](CV.md)

---