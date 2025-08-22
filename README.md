# Redis Dockerizer - Spring Boot Redis Integration

## Project Purpose

This project provides **modular implementations of four essential Redis integration scenarios** with Spring Boot:

1. **Cache Management** - Performance optimization by reducing database queries
2. **Key Management** - Managing temporary data (tokens, OTP, etc.)
3. **Pub/Sub** - Real-time messaging and event-driven architecture
4. **Session Management** - Centralized session management in distributed systems

**Project Approach:** Each Redis use case is developed as a separate module. Developers can **clone the project and choose the specific module** they need to directly integrate into their own projects or use as reference for further development.

This approach allows you to start working with **only the module you need** without having to learn all Redis implementations.

## System Requirements

- Java 17+
- Maven 3.6+
- Docker and Docker Compose
- Postman (for API testing)

## Installation Steps

### 1. Clone the Repository

```bash
git clone https://github.com/MenekseYuncu/redis-dockerizer.git
```
```bash
cd redis-dockerizer
```

### 2. Start Redis Container

Run Redis using Docker Compose:

```bash
docker-compose up -d
```

This command will start Redis on `localhost:6379`.

### 3. Install Maven Dependencies

```bash
./mvnw clean install
```

For Windows:
```bash
mvnw.cmd clean install
```

### 4. Start the Spring Boot Application

```bash
./mvnw spring-boot:run
```

The application will start running at `http://localhost:8082`.

### 5. Import Postman Collection

Use the prepared Postman collection to test API endpoints:

👉 [POSTMAN COLLECTION URL](https://www.postman.com/menekse-3683/workspace/redis-dockerizer/collection/24190370-5f0f8ac6-13e0-4983-aa1a-d1fd770f2d3d?action=share&source=copy-link&creator=24190370)

**Import Steps:**
1. Open Postman
2. Click "Import" button
3. Select "Link" tab
4. Paste the URL above
5. Click "Continue" and "Import" buttons

## Project Modules

### 1. Cache Management
- **Purpose:** Reduce database load by storing frequently accessed data in memory
- **Endpoints:**
  - `GET /api/cache/{id}` - Retrieve book from cache by ID
  - `POST /api/cache` - Create or update book in cache
  - `DELETE /api/cache/{id}` - Delete book from cache

### 2. Key Management
- **Purpose:** Store temporary data using Redis key-value structure
- **Endpoints:**
  - `POST /api/redis/set` - Store key-value pair with TTL support
  - `GET /api/redis/get/{key}` - Retrieve value by key
  - `DELETE /api/redis/del/{key}` - Delete key from Redis
  - `GET /api/redis/keys` - Get all keys (for debugging)
  - `POST /api/redis/expire/{key}` - Set TTL for existing key


### 3. Pub/Sub
- **Purpose:** Real-time messaging between services
- **Endpoints:**
  - `POST /api/pubsub/publish` - Publish structured message with sender info
  - `POST /api/pubsub/publish/simple` - Publish simple string message
  - `POST /api/pubsub/subscribe` - Subscribe to channel
  - `POST /api/pubsub/unsubscribe` - Unsubscribe from channel
  - `GET /api/pubsub/status` -  Check Pub/Sub service status


### 4. Session Management
- **Purpose:** Centrally manage user session information
- **Endpoints:**
  - `POST /api/session/login/{userId}` - Log in user and mark as online
  - `POST /api/session/logout/{userId}` - Log out user and mark as offline
  - `POST /api/session/refresh/{userId}` - Refresh user's online TTL
  - `GET /api/session/status/{userId}` - Get user online status and last active time
  - `GET /api/session/online` - Get all currently online users


## Testing

1. Ensure the application is running (`http://localhost:8082`)
2. Verify Redis container is active (`docker ps`)
3. Test endpoints sequentially using Postman collection
4. Run example requests for each module

## Project Structure

```
src/main/java/com/integration/redisdockerizer/
├── caching/                    # Cache Management module
├── keymanagement/              # Key Management module  
├── pubsub/                     # Pub/Sub module
├── session/                    # Session Management module(Online/Offline User)
└── RedisDockerizerApplication.java
```

## Configuration

Redis connection settings are defined in `src/main/resources/application.yml`:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## Technology Stack

- Java 17
- Spring Boot 3.x
- Spring Data Redis
- Redis 7.x
- Docker & Docker Compose
- Maven

## Detailed Documentation

For detailed explanations about Redis usage scenarios and Spring Boot integration covered in this project, you can read my Medium article (in Turkish):

**Medium Article:** [Redis ve Spring Boot: Modern Uygulamalarda Performans ve Ölçeklenebilirlik Rehberi](https://medium.com/@menekseyuncu/redis-spring-boot-article)

## Support

For questions about the project:
- You can use GitHub Issues
- Review API documentation in Postman collection
- Refer to detailed explanations in the Medium article
