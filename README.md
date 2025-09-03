# Redis Dockerizer – Spring Boot Redis Integration

## 🚀 Introduction

**redis-dockerizer** is a modular suite of **Redis-powered Spring Boot** projects, each showcasing a different integration pattern with **Docker**.
It can serve as both an educational playground and a starter template for real-world systems that need **speed, scalability, and simplicity**.

---

## 📄 Content

This repository currently includes four self-contained modules:

* **Pub/Sub** — real-time messaging with channel fan-out
* **Caching** — application-level caching for CRUD workloads
* **Key Management** — login, validation, extension, and logout using Redis-backed sessions
* **Session Management (Presence)** — online/offline tracking with TTL and session statistics

Each module includes a Spring Boot application, Docker Compose configuration for Redis, and sample REST endpoints.

---

## 💬 How to Use

1. **Clone the repository**

   ```bash
   git clone https://github.com/MenekseYuncu/redis-dockerizer.git
   cd redis-dockerizer
   ```

2. **Choose a module** (e.g., `pubsub`, `caching`, `key-management`, or `session-management`), then run:

   ```bash
   cd <module>
   docker-compose up -d    # start Redis
   ./mvnw spring-boot:run  # run the Spring Boot app
   ```

3. **Test the APIs** using cURL or Postman (see each module’s README for endpoints).

4. **Customize** TTLs, cache names, key strategies, and security settings as needed.

> **Requirements:** Java 21+, Maven 3.6+, Docker & Docker Compose

---

## 📦 Modules at a Glance

| Module                                          | Purpose                               | Highlights                                                                      |
| ----------------------------------------------- | ------------------------------------- | ------------------------------------------------------------------------------- |
| [**pubsub/**](./pubsub)                         | Real-time messaging via Redis Pub/Sub | Channel-based publish/subscribe, REST publishers, logging & metrics subscribers |
| [**caching/**](./caching)                       | Redis as a caching layer for CRUD     | `@Cacheable / @CachePut / @CacheEvict`, TTL, UUID `Product` API                 |
| [**key-management/**](./key-management)         | Session keys & login lifecycle        | Login, validate, extend, logout, terminate-all, client IP capture               |
| [**session-management/**](./session-management) | User presence (online/offline)        | Presence APIs, TTL refresh, session stats, user removal                         |

---

## 📂 Repository Structure

```
redis-dockerizer/
├── caching/
├── key-management/
├── pubsub/
├── session-management/
└── README.md
```

---

## 📬 Import Postman Collection

A complete Postman collection is available to test all modules’ APIs out of the box:

1. Open Postman
2. Import the collection using this link:
   👉 [Redis-Dockerizer Postman Collection](https://www.postman.com/menekse-3683/workspace/redis-dockerizer/overview)
3. Set the `baseUrl` variable according to the module you are running (e.g., `http://localhost:8082`, `http://localhost:8083`, etc.)
4. Explore endpoints grouped by module (Pub/Sub, Caching, Key Management, Session Management).

---

## 📚 Articles & Guides

* **(TR) Medium:** *Redis ve Spring Boot: Modern Uygulamalarda Performans ve Ölçeklenebilirlik Rehberi* — a comprehensive guide covering all modules.
  👉 [Read the article](https://medium.com/@menekseyuncu/redis-ve-spring-boot-modern-uygulamalarda-performans-ve-%C3%B6l%C3%A7eklenebilirlik-rehberi-f97bb85b2044)

---

## 🧑‍💻 Contributing

1. Fork the repository
2. Create a branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m "Add amazing feature"`)
4. Push the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 🙌 Acknowledgements

Thanks to everyone who has contributed to this repository.

Built with **Spring Boot**, **Spring Data Redis**, and **Docker**.

