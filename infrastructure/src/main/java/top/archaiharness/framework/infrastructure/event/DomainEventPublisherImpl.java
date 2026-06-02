package top.archaiharness.framework.infrastructure.event;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import top.archaiharness.framework.common.event.DomainEventPublisher;
import top.archaiharness.framework.common.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisherImpl implements DomainEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            publishToKafka(event);
            publishToLocal(event);
        }
    }

    @SuppressWarnings("null")
    private void publishToKafka(DomainEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            String topic = "event." + event.getEventType();

            kafkaTemplate.send(topic, event.getEventId(), json).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event to Kafka: topic={}, eventId={}", topic, event.getEventId(), ex);
                } else {
                    log.info("Published event to Kafka: topic={}, eventId={}", topic, event.getEventId());
                }
            });
        } catch (Exception e) {
            log.error("Failed to serialize event for Kafka: eventType={}, eventId={}", event.getEventType(), event.getEventId(), e);
        }
    }

    @SuppressWarnings("null")
    private void publishToLocal(DomainEvent event) {
        try {
            applicationEventPublisher.publishEvent(event);
            log.info("Published event to local Spring context: eventType={}, eventId={}", event.getEventType(), event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish event to local Spring context: eventType={}, eventId={}", event.getEventType(), event.getEventId(), e);
        }
    }
}