package top.archaiharness.framework.application.event;

import top.archaiharness.framework.common.event.DomainEvent;
import top.archaiharness.framework.common.event.annotation.OnEvent;
import top.archaiharness.framework.domain.user.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class UserCreatedEventHandler {

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @OnEvent(UserCreatedEvent.class)
    public void handle(DomainEvent event) {
        try {
            UserCreatedEvent userCreatedEvent = (UserCreatedEvent) event;
            String eventId = userCreatedEvent.getEventId();

            if (!processedEventIds.add(eventId)) {
                log.info("Event already processed, skipping: eventId={}", eventId);
                return;
            }

            log.info("Handling UserCreatedEvent: eventId={}, userId={}, username={}",
                     eventId, userCreatedEvent.getUserId(), userCreatedEvent.getUsername());

            handleUserCreated(userCreatedEvent);

            log.info("Successfully handled UserCreatedEvent: eventId={}", eventId);
        } catch (Exception e) {
            String eventId = event.getEventId();
            log.error("Failed to handle UserCreatedEvent: eventId={}", eventId, e);
            throw e;
        }
    }

    private void handleUserCreated(UserCreatedEvent event) {
        log.info("User created successfully: userId={}, username={}",
                 event.getUserId().toString(), event.getUsername());
    }
}