package com.redisdockerizer.sessionmanagement.session.dto;


/**
 * Represents the statistical information of user sessions, including total users,
 * number of users currently online, number of users offline, and the percentage of
 * users currently online.
 * <p>
 * This class is designed to encapsulate session-related statistics and provide
 * convenient methods for creating instances with calculated values.
 * <p>
 * The {@code SessionStatsResponse} is primarily used to convey session statistics
 * to other components or external systems.
 */
public record SessionStatsResponse(
        long totalUsers,
        long onlineUsers,
        long offlineUsers,
        double onlinePercentage
) {

    public static SessionStatsResponse of(long total, long online) {
        long offline = Math.max(0, total - online);
        double percentage = total > 0 ? (double) online / total * 100 : 0.0;
        return new SessionStatsResponse(
                total,
                online,
                offline,
                Math.round(percentage * 100.0) / 100.0
        );
    }
}