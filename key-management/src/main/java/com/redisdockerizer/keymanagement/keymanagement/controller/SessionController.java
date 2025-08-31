package com.redisdockerizer.keymanagement.keymanagement.controller;

import com.redisdockerizer.keymanagement.keymanagement.dto.*;
import com.redisdockerizer.keymanagement.keymanagement.exception.NotFoundException;
import com.redisdockerizer.keymanagement.keymanagement.exception.UnauthorizedException;
import com.redisdockerizer.keymanagement.keymanagement.model.UserSession;
import com.redisdockerizer.keymanagement.keymanagement.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The SessionController class provides RESTful APIs to manage user sessions.
 * It includes features such as login, session retrieval, session validation,
 * session extension, session termination, and management of active sessions.
 * <p>
 * It acts as the entry point for session-related operations in the system
 * and communicates with the SessionService to perform the underlying logic.
 */
@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * Handles the login request by validating the user's credentials
     * and initiating a session if authentication is successful.
     *
     * @param request the LoginRequest object containing the user's credentials (username and password)
     * @param httpRequest the HttpServletRequest object containing request metadata, including client IP address
     * @return a LoginResponse object containing session details, user information, and a status message
     * @throws UnauthorizedException if the credentials are invalid or the user is inactive
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletRequest httpRequest) {

        String clientIp = clientIp(httpRequest);

        return sessionService.createSession(request.username(), request.password(), clientIp)
                .map(session -> new LoginResponse(
                        session.getSessionId(),
                        session.getUserId(),
                        session.getUsername(),
                        session.getRoles(),
                        session.getExpiresAt(),
                        "Login successful"
                ))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials or inactive user"));
    }


    /**
     * Retrieves the details of a specific user session based on the provided session ID.
     * This method fetches the session information if it exists and is valid; otherwise,
     * it throws a NotFoundException if the session is missing or expired.
     *
     * @param sessionId the unique identifier of the session to be retrieved
     * @return a SessionDetailsResponse containing detailed information about the session,
     * including session ID, user ID, username, roles, creation and expiration times, and client IP
     * @throws NotFoundException if the session is not found or has expired
     */
    @GetMapping("/{sessionId}")
    public SessionDetailsResponse getSession(@PathVariable String sessionId) {
        UserSession session = sessionService.getSession(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found or expired"));

        return new SessionDetailsResponse(
                session.getSessionId(),
                session.getUserId(),
                session.getUsername(),
                session.getRoles(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.getClientIp()
        );
    }


    /**
     * Logs out a user session by deleting the session identified by the given session ID.
     * If the session does not exist, a NotFoundException is thrown.
     *
     * @param sessionId the unique identifier of the session to be logged out
     * @return a MessageResponse indicating the success of the logout operation,
     *         including the session ID in the response data
     * @throws NotFoundException if the session is not found
     */
    @DeleteMapping("/{sessionId}/logout")
    public MessageResponse logout(@PathVariable String sessionId) {
        boolean deleted = sessionService.deleteSession(sessionId);
        if (!deleted) {
            throw new NotFoundException("Session not found");
        }
        return new MessageResponse("Logout successful", Map.of("sessionId", sessionId));
    }


    /**
     * Retrieves all currently active session identifiers.
     * This method delegates to the session service to fetch active session data
     * and wraps the results into an ActiveSessionsResponse object.
     *
     * @return an ActiveSessionsResponse containing the set of active session IDs
     *         and the total count of active sessions
     */
    @GetMapping("/active")
    public ActiveSessionsResponse getAllActiveSessions() {
        Set<String> activeSessions = sessionService.getAllActiveSessions();
        return new ActiveSessionsResponse(activeSessions, activeSessions.size());
    }


    /**
     * Retrieves all active sessions associated with a specific user based on the provided user ID.
     * This method queries the session service to fetch active sessions for the user and returns them
     * in a structured response that includes session details and the total count of sessions.
     *
     * @param userId the unique identifier of the user for whom active sessions are to be retrieved
     * @return a UserSessionsResponse containing the user ID, a list of active sessions,
     *         and the total number of active sessions
     */
    @GetMapping("/user/{userId}")
    public UserSessionsResponse getUserSessions(@PathVariable Long userId) {
        List<String> userSessions = sessionService.getUserActiveSessions(userId);
        return new UserSessionsResponse(userId, userSessions, userSessions.size());
    }


    /**
     * Extends the duration of an existing session by a specified number of minutes.
     * If the session is not found or has expired, a NotFoundException is thrown.
     *
     * @param sessionId the unique identifier of the session to be extended
     * @param minutes the number of minutes to extend the session, defaulting to 30 if not provided
     * @return an ExtendSessionResponse containing a message, the session ID,
     *         and the updated duration in minutes
     * @throws NotFoundException if the session is not found or has expired
     */
    @PutMapping("/{sessionId}/extend")
    public ExtendSessionResponse extendSession(@PathVariable String sessionId,
                                               @RequestParam(defaultValue = "30") int minutes) {
        boolean extended = sessionService.extendSession(sessionId, minutes);
        if (!extended) {
            throw new NotFoundException("Session not found or expired");
        }
        return new ExtendSessionResponse("Session extended successfully", sessionId, minutes);
    }


    /**
     * Terminates all active sessions for a specific user based on the provided user ID.
     * This operation deletes all session data associated with the user and ensures
     * that the user is effectively logged out from all active sessions.
     *
     * @param userId the unique identifier of the user whose sessions are to be terminated
     * @return a TerminateAllResponse containing a confirmation message, the user ID,
     *         and the total count of sessions that were terminated
     */
    @DeleteMapping("/user/{userId}/terminate-all")
    public TerminateAllResponse terminateAllUserSessions(@PathVariable Long userId) {
        int terminatedCount = sessionService.terminateAllUserSessions(userId);
        return new TerminateAllResponse("User sessions terminated", userId, terminatedCount);
    }


    /**
     * Validates a user session based on the provided session ID.
     * If the session exists and is valid, returns session details.
     * Otherwise, throws an UnauthorizedException for invalid or expired sessions.
     *
     * @param sessionId the unique identifier of the session to validate
     * @return a ValidateSessionResponse containing the session validation status,
     *         the user ID, username, associated roles, and an optional error message
     * @throws UnauthorizedException if the session is invalid or expired
     */
    @PostMapping("/validate")
    public ValidateSessionResponse validateSession(@RequestParam String sessionId) {
        return sessionService.getSession(sessionId)
                .map(s -> new ValidateSessionResponse(true, s.getUserId(), s.getUsername(), s.getRoles(), null))
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired session"));
    }


    /**
     * Extracts the IP address of the client making the request.
     * It first checks the "X-Forwarded-For" header for the client's IP, which may contain
     * multiple IPs if the request passed through proxies. The method uses the first IP in the list.
     * If "X-Forwarded-For" is unavailable or empty, it falls back to the "X-Real-IP" header.
     * Finally, if both headers are unavailable or empty, it retrieves the remote address
     * directly from the request.
     *
     * @param request the HttpServletRequest object from which the client IP will be extracted
     * @return the client's IP address as a String; can be from "X-Forwarded-For", "X-Real-IP",
     *         or the remote address of the request
     */
    private String clientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}