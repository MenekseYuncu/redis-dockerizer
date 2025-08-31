package com.redisdockerizer.keymanagement.keymanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redisdockerizer.keymanagement.keymanagement.model.User;
import com.redisdockerizer.keymanagement.keymanagement.model.UserSession;
import com.redisdockerizer.keymanagement.keymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSION_PREFIX = "user_sessions:";
    private static final Duration DEFAULT_SESSION_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new user session if the provided credentials are valid. The session
     * will be associated with the given username and client IP address.
     *
     * @param username the username of the user attempting to create a session
     * @param password the password associated with the user
     * @param clientIp the client IP address initiating the session
     * @return an {@code Optional} containing the {@code UserSession} if the credentials
     *         are valid and the session is successfully created; {@code Optional.empty()}
     *         otherwise
     */
    public Optional<UserSession> createSession(String username, String password, String clientIp) {
        if (!userRepository.validateCredentials(username, password)) {
            return Optional.empty();
        }

        return userRepository.findByUsername(username)
                .map(user -> newSessionFor(user, clientIp))
                .map(session -> {
                    persistSession(session, DEFAULT_SESSION_TTL);
                    addUserSessionMapping(session.getUserId(), session.getSessionId());
                    return session;
                });
    }

    /**
     * Retrieves a user session based on the provided session ID. If the session exists and is not expired,
     * it is returned as an {@code Optional<UserSession>}. If the session is expired, it is deleted and an
     * empty {@code Optional} is returned.
     *
     * @param sessionId the unique identifier of the session to retrieve
     * @return an {@code Optional} containing the {@code UserSession} if the session is active and valid;
     *         otherwise, {@code Optional.empty()}
     */
    public Optional<UserSession> getSession(String sessionId) {
        return readSession(sessionId)
                .filter(this::notExpired)
                .or(() -> {
                    deleteSession(sessionId);
                    return Optional.empty();
                });
    }

    /**
     * Deletes a user session based on the provided session ID. The session data is removed from the storage,
     * and the user-session mapping is also cleaned up if it exists.
     *
     * @param sessionId the unique identifier of the session to be deleted
     * @return {@code true} if the session was successfully deleted; {@code false} otherwise
     */
    public boolean deleteSession(String sessionId) {
        Optional<UserSession> sessionOpt = readSession(sessionId);
        String sessionKey = sessionKey(sessionId);

        Boolean deleted = redisTemplate.delete(sessionKey);
        sessionOpt.ifPresent(s -> removeUserSessionMapping(s.getUserId(), sessionId));

        return Boolean.TRUE.equals(deleted);
    }

    /**
     * Retrieves all currently active session identifiers from the session storage.
     * Active sessions are identified by a specific prefix in the storage keys.
     *
     * @return a {@code Set<String>} containing all active session IDs, sorted in natural order
     */
    public Set<String> getAllActiveSessions() {
        Set<String> keys = Optional.of(redisTemplate.keys(SESSION_PREFIX + "*"))
                .orElseGet(Collections::emptySet);

        return keys.stream()
                .map(this::stripSessionPrefix)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Retrieves a list of active session IDs for a given user.
     * A session is considered active if it exists and is not expired.
     * Expired sessions will be removed during this process.
     *
     * @param userId the unique identifier of the user whose active sessions are to be retrieved
     * @return a list of active session IDs associated with the specified user, sorted in natural order;
     *         returns an empty list if no active sessions are found or if the user has no associated sessions
     */
    public List<String> getUserActiveSessions(Long userId) {
        Set<Object> raw = redisTemplate.opsForSet().members(userSessionsKey(userId));
        if (raw == null || raw.isEmpty()) return Collections.emptyList();

        List<String> ids = raw.stream().map(Object::toString).sorted().toList();

        List<String> alive = new ArrayList<>(ids.size());
        for (String id : ids) {
            if (getSession(id).isPresent()) {
                alive.add(id);
            } else {
                redisTemplate.opsForSet().remove(userSessionsKey(userId), id);
            }
        }
        return alive;
    }

    /**
     * Extends the duration of an existing user session by a specified number of minutes.
     * If the session does not exist, is expired, or the additional duration is invalid,
     * the operation will fail.
     *
     * @param sessionId the unique identifier of the session to be extended
     * @param additionalMinutes the number of additional minutes to extend the session's expiration
     * @return {@code true} if the session was successfully extended; {@code false} if the session
     *         does not exist, is expired, or if the additionalMinutes is less than or equal to zero
     */
    public boolean extendSession(String sessionId, int additionalMinutes) {
        if (additionalMinutes <= 0) return false;

        Optional<UserSession> sessionOpt = readSession(sessionId);
        if (sessionOpt.isEmpty()) return false;

        UserSession session = sessionOpt.get();
        if (isExpired(session)) {
            deleteSession(sessionId);
            return false;
        }

        LocalDateTime newExpiresAt = session.getExpiresAt().plusMinutes(additionalMinutes);
        session.setExpiresAt(newExpiresAt);

        Duration remaining = Duration.between(LocalDateTime.now(), newExpiresAt);
        if (remaining.isNegative() || remaining.isZero()) {
            deleteSession(sessionId);
            return false;
        }

        persistSession(session, remaining);
        return true;
    }

    /**
     * Terminates all active user sessions associated with the given user ID.
     * For each session ID retrieved, an attempt is made to delete the session.
     * If successful, the session is counted as terminated.
     *
     * @param userId the unique identifier of the user whose sessions are to be terminated
     * @return the number of user sessions that were successfully terminated
     */
    public int terminateAllUserSessions(Long userId) {
        List<String> ids = getUserActiveSessions(userId);
        int count = 0;
        for (String id : ids) {
            if (deleteSession(id)) count++;
        }
        return count;
    }


    /**
     * Creates a new {@code UserSession} for the specified user and client IP address.
     * The session includes a unique session ID, user details, creation time, expiration time, and client IP.
     *
     * @param user the {@code User} for whom the session is being created
     * @param clientIp the IP address of the client initiating the session
     * @return a new {@code UserSession} object containing the session details
     */
    private UserSession newSessionFor(User user, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(DEFAULT_SESSION_TTL);

        return new UserSession(
                generateSessionId(),
                user.getId(),
                user.getUsername(),
                user.getRoles(),
                now,
                expiresAt,
                clientIp
        );
    }

    /**
     * Persists a user session into the data store with a specified time-to-live (TTL).
     *
     * @param session the user session to be persisted
     * @param ttl the duration for which the session should be valid before expiration
     */
    private void persistSession(UserSession session, Duration ttl) {
        redisTemplate.opsForValue().set(
                sessionKey(session.getSessionId()),
                session,
                ttl.toMinutes(),
                TimeUnit.MINUTES
        );
    }

    /**
     * Reads and retrieves a user session associated with the given session ID from the data store.
     *
     * @param sessionId the unique identifier of the session to be read
     * @return an {@code Optional} containing the {@code UserSession} if found and successfully retrieved;
     *         otherwise, an empty {@code Optional} if the session does not exist or cannot be converted
     */
    private Optional<UserSession> readSession(String sessionId) {
        Object raw = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (raw == null) return Optional.empty();

        if (raw instanceof UserSession us) {
            return Optional.of(us);
        }
        try {
            return Optional.of(objectMapper.convertValue(raw, UserSession.class));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /**
     * Associates a user ID with a session ID in a Redis data store.
     *
     * @param userId the unique identifier of the user
     * @param sessionId the unique identifier of the user's session
     */
    private void addUserSessionMapping(Long userId, String sessionId) {
        redisTemplate.opsForSet().add(userSessionsKey(userId), sessionId);
    }

    /**
     * Removes the mapping of a user's session from the session store.
     *
     * @param userId the ID of the user whose session mapping needs to be removed
     * @param sessionId the ID of the session to be removed from the user's session mappings
     */
    private void removeUserSessionMapping(Long userId, String sessionId) {
        redisTemplate.opsForSet().remove(userSessionsKey(userId), sessionId);
    }

    /**
     * Checks if the given user session has not expired.
     *
     * @param s the user session to check
     * @return true if the user session is not expired, false otherwise
     */
    private boolean notExpired(UserSession s) {
        return !isExpired(s);
    }

    /**
     * Checks whether the given user session has expired based on its expiration time.
     *
     * @param s the user session to check for expiration
     * @return true if the session has expired, false otherwise
     */
    private boolean isExpired(UserSession s) {
        return s.getExpiresAt() != null && s.getExpiresAt().isBefore(LocalDateTime.now());
    }

    /**
     * Constructs a session key by appending the provided session ID to a predefined session prefix.
     *
     * @param sessionId the unique identifier for the session
     * @return the constructed session key as a combination of the session prefix and session ID
     */
    private String sessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    /**
     * Generates a session key string for a given user based on their unique ID.
     *
     * @param userId the unique identifier of the user
     * @return a concatenated string representing the user's session key
     */
    private String userSessionsKey(Long userId) {
        return USER_SESSION_PREFIX + userId;
    }

    /**
     * Removes the session prefix from the given Redis key.
     *
     * @param redisKey The Redis key from which the session prefix should be removed.
     * @return The Redis key without the session prefix.
     */
    private String stripSessionPrefix(String redisKey) {
        return redisKey.substring(SESSION_PREFIX.length());
    }

    /**
     * Generates a new unique session identifier.
     *
     * @return A string representing the generated session ID, prefixed with "sess_"
     *         followed by a UUID with dashes removed.
     */
    private String generateSessionId() {
        return "sess_" + UUID.randomUUID().toString().replace("-", "");
    }
}