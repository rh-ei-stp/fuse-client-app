package org.redhat.integration.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.MessageHeaders;

@Slf4j
public class EventListener {

    @KafkaListener(id = "amq-client-event-listener",
            topics = "${kafka.listen.topic.name}",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "${kafka.listen.auto-start:false}",
            groupId = "amq-client-event-listener-group-id")
    public void eventListener(byte[] eventBytes, Acknowledgment acknowledgment, MessageHeaders headers) {
        log.info("Message! Cool.");
    }
}
