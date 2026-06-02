package top.archaiharness.framework.infrastructure.event;

import top.archaiharness.framework.common.event.DomainEvent;
import top.archaiharness.framework.common.event.annotation.OnEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class EventDispatcher {

    private final Map<Class<? extends DomainEvent>, EventHandlerMethod> handlerByClass = new HashMap<>();
    private final Map<String, Class<? extends DomainEvent>> eventTypeToClassMap = new HashMap<>();

    public EventDispatcher() {
    }

    @SuppressWarnings("null")
    @EventListener
    public void onContextRefreshed(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            Method[] methods = targetClass.getDeclaredMethods();

            for (Method method : methods) {
                OnEvent onEvent = method.getAnnotation(OnEvent.class);
                if (onEvent != null) {
                    Class<? extends DomainEvent> eventType = onEvent.value();
                    EventHandlerMethod handler = new EventHandlerMethod(bean, method);
                    handlerByClass.put(eventType, handler);
                    
                    String eventTypeStr = eventType.getSimpleName().replace("Event", "");
                    eventTypeToClassMap.put(eventTypeStr, eventType);
                    
                    log.info("Registered event handler: {}#{} for {}",
                        targetClass.getSimpleName(), method.getName(), eventType.getSimpleName());
                }
            }
        }
    }

    public void dispatch(DomainEvent event) {
        EventHandlerMethod handler = handlerByClass.get(event.getClass());
        if (handler == null) {
            log.warn("No handler found for event: {}", event.getClass().getSimpleName());
            return;
        }
        try {
            handler.invoke(event);
        } catch (Exception e) {
            log.error("Failed to handle event: {}", event.getClass().getSimpleName(), e);
            throw e;
        }
    }

    public Map<String, Class<? extends DomainEvent>> getEventTypeToClassMap() {
        return eventTypeToClassMap;
    }

    private record EventHandlerMethod(Object bean, Method method) {
        public void invoke(DomainEvent event) {
            try {
                method.invoke(bean, event);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke event handler", e);
            }
        }
    }
}
