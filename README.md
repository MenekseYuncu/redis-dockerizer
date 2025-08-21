# Redis Dockerizer - Spring Boot & Redis Integration Demo

## Project Overview

This project demonstrates **Redis integration patterns with Spring Boot** through a comprehensive example application. It contains micro-modules showcasing different Redis usage scenarios (Caching, Key Management, Pub/Sub, Session Management) with **Docker Compose** setup for easy Redis installation and a ready-to-use **Postman Collection** for testing.

---

## Project Structure

```
redis-dockerizer-main
|
├── src/main/java/com/integration/redisdockerizer
│   ├── caching/                      # Redis Cache examples
│   ├── keymanagement/                # Redis Key-Value operations
│   ├── pubsub/                       # Publish/Subscribe implementation
│   ├── session/                      # Session & Online Users management
│   └── RedisDockerizerApplication.java
└── src/main/resources/application.yml
├── docker-compose.yml                # Redis container configuration
├── pom.xml                           # Maven dependencies
```

---

## Project Setup

Follow these steps to run the project on your local machine:

### 1. Clone the Repository

```bash
git clone https://github.com/MenekseYuncu/redis-dockerizer.git
```
```bash
cd redis-dockerizer
```

### 2. Start Redis with Docker

The project includes a ready-to-use `docker-compose.yml` file for quick Redis setup.

```bash
docker-compose up -d
```

✔️ Redis is now running on **localhost:6379**

### 3. Install Maven Dependencies

```bash
./mvnw clean install
```

(Windows: `mvnw.cmd clean install`)

### 4. Start the Spring Boot Application

```bash
./mvnw spring-boot:run
```

✔️ Application will run on `http://localhost:8082` by default

### 5. Import the Postman Collection

Use the prepared Postman collection to test all endpoints:

👉 [Redis Dockerizer - Integration API Collection](https://www.postman.com/menekse-3683/workspace/redis-dockerizer/collection/24190370-5f0f8ac6-13e0-4983-aa1a-d1fd770f2d3d?action=share&source=copy-link&creator=24190370)

* Open Postman
* Click **Import** → **Link** → Paste the URL above
* You can now test all endpoints 

---

## Redis Modules

### 1. **Cache Management**

* **Purpose:** Store frequently accessed data in memory (Redis) to prevent repeated database queries
* **Scenario:** Book list data is stored in the database. On first request, data is fetched from the database and cached in Redis. Subsequent requests retrieve data directly from Redis
* **Implementation:**
  * `BookEntity` → Cacheable model
  * `CacheController` → Book addition/retrieval operations
  * `CacheService` → Redis cache management
* **Advantage:** High performance, reduced database load

---

### 2. **Key Management**

* **Purpose:** Demonstrate Redis's core **key-value store** functionality
* **Scenario:** Store user settings or temporary data (e.g., verification codes, tokens) in Redis
* **Implementation:**
  * `RedisController` → set/get/delete operations
  * `RedisService` → Manages operations using RedisTemplate
* **Advantage:** Simple and fast key-value management with dynamic data storage capability

---

### 3. **Pub/Sub (Publish/Subscribe)**

* **Purpose:** Enable **real-time messaging** between services
* **Scenario:** One service publishes messages while others subscribe to the same channel to receive messages instantly. Used in chat applications, notification systems, and event-driven architectures
* **Implementation:**
  * `PubSubController` → Message publishing endpoint
  * `RedisPublisher` → Publishes messages
  * `RedisSubscriber` → Listens for messages
  * `RedisPubSubConfig` → Redis channel configuration
* **Advantage:** Enables scalable, event-driven architecture in distributed systems

---

### 4. **Session Management**

* **Purpose:** Centralized user session management
* **Scenario:** When multiple backend instances are running, user session information shouldn't be confined to a single instance. Redis enables centralized session storage
* **Implementation:**
  * `RedisSessionConfig` → Session management configuration
  * `OnlineUserController` → Lists online/offline users
  * `OnlineUserService` → Manages user status in Redis
  * `UserEntity` → User model structure
* **Advantage:**
  * Users see the same session information regardless of which server they connect to
  * Simplified online/offline user tracking
  * Scalable session management in distributed systems

---

## Step-by-Step Workflow Example

### 1. Infrastructure Setup
```bash
# Start Redis container
docker-compose up -d

# Verify Redis is running
docker ps

# Expected output should show redis container with status 'Up'
```

### 2. Application Startup
```bash
# Build and run the Spring Boot application
./mvnw clean spring-boot:run

# Watch for successful startup message:
# 'Started RedisDockerizerApplication in X.XXX seconds'
```

### 3. Testing with Postman

#### Cache Management Testing:
1. **Add Book**: POST to `/api/cache/books` with JSON body:
   ```json
   {
     "id": 1,
     "title": "Redis in Action",
     "author": "Josiah L. Carlson"
   }
   ```
2. **Get Book**: GET to `/api/cache/books/1` - First call hits database, subsequent calls served from cache

#### Key Management Testing:
1. **Set Value**: POST to `/api/redis/set` with parameters:
   - key: "testKey"
   - value: "testValue"
   - ttl: 300 (optional)
2. **Get Value**: GET to `/api/redis/get/testKey`
3. **Delete Value**: DELETE to `/api/redis/delete/testKey`

#### Pub/Sub Testing:
1. **Subscribe**: The application automatically subscribes to "pubsub-channel" on startup
2. **Publish Message**: POST to `/api/pubsub/publish` with body:
   ```json
   {
     "message": "Hello Redis Pub/Sub!"
   }
   ```
3. **Check Logs**: Message appears in application console output

#### Session Management Testing:
1. **Login User**: POST to `/api/session/login` with parameter:
   - username: "testUser"
2. **Check Online Users**: GET to `/api/session/online-users`
3. **Logout User**: POST to `/api/session/logout` with parameter:
   - username: "testUser"

### 4. Monitoring and Verification
```bash
# Check Redis data (optional - requires redis-cli)
docker exec -it redis-dockerizer-redis-1 redis-cli

# Inside redis-cli:
KEYS *        # View all keys
MONITOR       # Real-time operation monitoring
```

### 5. Expected Results
- **Cache Module**: Second book retrieval should be significantly faster (check response headers for `X-Cache: HIT`)
- **Key Module**: Set values should persist until TTL expiration
- **Pub/Sub Module**: Published messages should appear in application logs
- **Session Module**: User status should update immediately across all instances

---

## Technical Stack

- **Java 17** - Programming language
- **Spring Boot 3.x** - Application framework
- **Redis** - In-memory data store
- **Docker & Docker Compose** - Containerization
- **Maven** - Dependency management
- **Postman** - API testing

---

##  Support

If you have any questions or need assistance:

1. Check the API documentation in Postman
2. Review the code comments for each module
3. Open an issue on GitHub with detailed description
