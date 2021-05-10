package org.redhat.integration.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class TopicPublisher {

    @Value(value = "${kafka.listen.topic.name}") private String topicName;

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Scheduled(fixedRateString = "${kafka.publisher.fixedDelay:10000}")
    public void sendTopicMessage(){
        send("Hello World");
    }

    private SendResult<String, byte[]> send(String message) {
        try {
            //get makes this send call a blocking call.
            return  kafkaTemplate.send(topicName, message.getBytes()).get(10, TimeUnit.SECONDS);
        }catch (KafkaException | TimeoutException |InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
