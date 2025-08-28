# Redis Dockerizer

Spring Boot application integrated with Redis - featuring Key Management, Pub/Sub and Session Management capabilities.

## 🚨 Security Warning

The following security measures **MUST** be implemented before using this application in production:

- Redis encryption must be enabled
- SSL/TLS must be used
- Authentication/Authorization must be implemented
- Rate limiting must be added
- Input validation must be implemented

## 🛡️ Security Features

- ✅ Redis container security (non-root user, bind 127.0.0.1)
- ✅ Input validation and sanitization
- ✅ CORS configuration
- ✅ Spring Security integration
- ✅ Connection pooling
- ✅ Timeout settings

## 🚀 Installation

### 1. Environment Variables
```bash
cp env.example .env
# Edit .env file and set secure passwords
```

### 2. Docker Compose
```bash
docker-compose up -d
```

### 3. Application
```bash
mvn spring-boot:run
```

## 📋 API Endpoints

### Redis Key Management
- `POST /api/redis/set` - Key-value set
- `GET /api/redis/get/{key}` - Key-value get
- `DELETE /api/redis/del/{key}` - Key delete
- `GET /api/redis/keys` - All keys
- `POST /api/redis/expire/{key}` - Set TTL

### Pub/Sub
- `POST /api/pubsub/publish` - Message publish
- `POST /api/pubsub/subscribe` - Channel subscribe
- `POST /api/pubsub/unsubscribe` - Channel unsubscribe

### Session Management
- `POST /api/session/login/{userId}` - User login
- `POST /api/session/logout/{userId}` - User logout
- `GET /api/session/status/{userId}` - User status
- `GET /api/session/online` - Online users

## 🔒 Security Checklist

- [ ] Redis password changed
- [ ] SSL certificate added
- [ ] JWT authentication implemented
- [ ] Rate limiting added
- [ ] Logging and monitoring added
- [ ] Security headers added

## 📝 Notes

- Redis is bound to localhost by default (127.0.0.1:6379)
- All API endpoints are protected with input validation
- CORS only allows localhost origins
- Connection timeout is set to 30 seconds
