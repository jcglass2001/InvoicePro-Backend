package com.jcg.bankingapi.repository.implementation;

import com.jcg.bankingapi.domain.UserEvent;
import com.jcg.bankingapi.domain.enums.EventType;
import com.jcg.bankingapi.repository.EventRepository;
import com.jcg.bankingapi.rowMapper.UserEventRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

import static com.jcg.bankingapi.query.EventQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventRepositoryImpl implements EventRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    @Override
    public Collection<UserEvent> getEventsByUserId(Long userId) {
        return jdbc.query(SELECT_EVENTS_BY_USER_ID_QUERY, Map.of("userId", userId), new UserEventRowMapper());
    }

    @Override
    public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {
        jdbc.update(INSERT_EVENT_BY_USER_EMAIL_QUERY,
                Map.of("email", email,
                        "type", eventType.toString(),
                        "device",device,
                        "ipAddress", ipAddress)
        );
    }

    @Override
    public void addUserEvent(Long userId, EventType eventType, String device, String ipAddress) {

    }
}
