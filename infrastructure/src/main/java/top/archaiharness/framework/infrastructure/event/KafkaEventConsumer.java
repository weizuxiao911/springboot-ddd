package top.archaiharness.framework.infrastructure.event;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import top.archaiharness.framework.common.event.DomainEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaEventConsumer {

    private final EventDispatcher eventDispatcher;
    private final ObjectMapper objectMapper;
    private final Map<String, Class<? extends DomainEvent>> eventTypeToClassMap;

    public KafkaEventConsumer(EventDispatcher eventDispatcher, ObjectMapper objectMapper) {
        this.eventDispatcher = eventDispatcher;
        this.objectMapper = objectMapper;
        this.eventTypeToClassMap = eventDispatcher.getEventTypeToClassMap();
        log.info("Loaded {} event type mappings from EventDispatcher", eventTypeToClassMap.size());
    }

    @KafkaListener(topicPattern = "event\\..+", groupId = "${spring.application.name:framework}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String json = record.value();
        String topic = record.topic();
        log.info("Received Kafka event: topic={}, key={}", topic, record.key());

        try {
            JsonNode node = objectMapper.readTree(json);
            String eventType = node.has("eventType") ? node.get("eventType").asText() : null;
            
            if (eventType == null) {
                log.error("Missing eventType field in Kafka message: topic={}", topic);
                ack.acknowledge();
                return;
            }

            Class<? extends DomainEvent> eventClass = eventTypeToClassMap.get(eventType);
            if (eventClass == null) {
                log.error("Unknown event type: {}", eventType);
                ack.acknowledge();
                return;
            }
            
            DomainEvent event = deserializeEvent(node, eventClass);
            event.setEventId(record.key());

            eventDispatcher.dispatch(event);

            log.info("Kafka event dispatched: eventType={}, eventType={}", event.getEventType(), event.getEventId());
        } catch (Exception e) {
            log.error("Failed to process Kafka event: topic={}", topic, e);
            throw new RuntimeException(e);
        } finally {
            ack.acknowledge();
        }
    }

    private DomainEvent deserializeEvent(JsonNode node, Class<? extends DomainEvent> eventClass) throws Exception {
        Constructor<?>[] constructors = eventClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramType = paramTypes[i];
                if (paramType == String.class) {
                    String paramName = getParamName(constructor, i);
                    if (paramName.isEmpty()) {
                        continue;
                    }
                    if (node.has(paramName)) {
                        args[i] = node.get(paramName).asText();
                    }
                }
            }
            
            constructor.setAccessible(true);
            return (DomainEvent) constructor.newInstance(args);
        }
        throw new RuntimeException("No suitable constructor found for class: " + eventClass.getName());
    }

    private String getParamName(Constructor<?> constructor, int paramIndex) {
        java.lang.reflect.Parameter param = constructor.getParameters()[paramIndex];
        if (param.isNamePresent()) {
            String name = param.getName();
            log.debug("Parameter name found: {}", name);
            return name;
        }
        log.warn("Parameter name not available for {} index {}", constructor.getName(), paramIndex);
        return "";
    }
}