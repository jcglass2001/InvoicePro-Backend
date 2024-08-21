package com.jcg.bankingapi.event;

import com.jcg.bankingapi.domain.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class NewUserEvent extends ApplicationEvent {
    private EventType eventType;
    private String email;

    public NewUserEvent(EventType type, String email) {
        super(email);
        this.eventType = type;
        this.email = email;
    }
}
