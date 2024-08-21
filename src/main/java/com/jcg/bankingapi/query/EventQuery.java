package com.jcg.bankingapi.query;

public class EventQuery {
    public static final String SELECT_EVENTS_BY_USER_ID_QUERY =
            "SELECT uev.id, uev.device, uev.ip_address, uev.created_at, ev.event_type, ev.description " +
                    "FROM Events ev " +
                    "JOIN UserEvents uev ON ev.id = uev.event_id " +
                    "JOIN Users u ON u.id = uev.user_id " +
                    "WHERE u.id = :userId ORDER BY uev.created_at DESC LIMIT 10";
    public static final String INSERT_EVENT_BY_USER_EMAIL_QUERY =
            "INSERT INTO UserEvents (user_id, event_id, device, ip_address)" +
                    "VALUES (" +
                        "(SELECT id FROM Users WHERE email = :email), " +
                        "(SELECT id FROM Events WHERE event_type = :type), " +
                        ":device, :ipAddress " +
                    ")";
}
