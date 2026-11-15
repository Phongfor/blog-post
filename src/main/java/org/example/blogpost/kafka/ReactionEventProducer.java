package org.example.blogpost.kafka;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.event.ReactionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReactionEventProducer {

    KafkaTemplate<String, String> kafkaTemplate;
    String topic;

    public ReactionEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                                 @Value("${app.kafka.reaction-topic:reaction-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReaction(ReactionEvent event) {
        try {
            kafkaTemplate.send(topic, String.valueOf(event.postId()), event.toMessage())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish reaction event: postId={}, actorId={}",
                                    event.postId(), event.actorId(), ex);
                        }
                    });
        } catch (RuntimeException e) {
            log.error("Failed to publish reaction event: postId={}, actorId={}",
                    event.postId(), event.actorId(), e);
        }
    }
}