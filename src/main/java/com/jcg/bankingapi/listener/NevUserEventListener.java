package com.jcg.bankingapi.listener;

import com.jcg.bankingapi.event.NewUserEvent;
import com.jcg.bankingapi.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.jcg.bankingapi.utils.RequestUtils.getDevice;
import static com.jcg.bankingapi.utils.RequestUtils.getIpAddress;

@Component
@RequiredArgsConstructor
public class NevUserEventListener {
    private final EventService eventService;
    private final HttpServletRequest request;

    @EventListener
    public void onNewUserEvent(NewUserEvent event){
        eventService.addUserEvent(event.getEmail(), event.getEventType(), getDevice(request), getIpAddress(request));
    }
}
