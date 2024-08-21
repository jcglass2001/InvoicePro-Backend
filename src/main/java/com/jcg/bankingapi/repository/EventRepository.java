package com.jcg.bankingapi.repository;

import com.jcg.bankingapi.domain.UserEvent;
import com.jcg.bankingapi.domain.enums.EventType;

import java.util.Collection;

public interface EventRepository {
    Collection<UserEvent> getEventsByUserId(Long userId);
    void addUserEvent(String email, EventType eventType, String device, String ipAddress);
    void addUserEvent(Long userId, EventType eventType, String device, String ipAddress);
}
