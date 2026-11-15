package org.example.blogpost.kafka;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.event.ReactionEvent;
import org.example.blogpost.service.ReactionNotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReactionEventConsumer {

    ReactionNotificationService reactionNotificationService;

    @KafkaListener(topics = "${app.kafka.reaction-topic:reaction-events}")
    public void handle(String message) {
        ReactionEvent event;
        try {
            event = ReactionEvent.fromMessage(message);
        } catch (RuntimeException e) {
            // Message hỏng thì thử lại cũng vô ích, bỏ qua
            log.warn("Skip malformed reaction event: {}", message);
            return;
        }
        // Lỗi gửi mail được ném lên để DefaultErrorHandler thử lại
        reactionNotificationService.notifyPostOwner(event);
    }
}