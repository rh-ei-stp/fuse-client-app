package org.redhat.integration.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;

import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {


    @Value(value = "${kafka.bootstrap-address}")
    private String bootstrapAddress;
    @Value(value = "${kafka.enable-secure-kafka:true}")
    private boolean enableSecureKafka;
    @Value(value = "${kafka.protocol}")
    private String protocol;


    @Value(value = "${kafka.keystore.location}")
    private String keystoreLocation;
    @Value(value = "${kafka.keystore.password}")
    private String keystorePassword;

    @Bean
    public ProducerFactory<String, byte[]> producerFactory() throws IOException {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        configProps.put("security.protocol", protocol);

        if(enableSecureKafka){
            configProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreLocation);
            configProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
            configProps.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keystorePassword);
        }

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate() throws IOException {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, byte[]> consumerFactory() throws IOException {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "amq-client");

        props.put("security.protocol", protocol);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        if(enableSecureKafka){
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreLocation);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keystorePassword);
        }

        return new DefaultKafkaConsumerFactory<>(props, new ErrorHandlingDeserializer<String>(new StringDeserializer()), new ErrorHandlingDeserializer<byte[]>(new ByteArrayDeserializer()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerContainerFactory(ErrorHandler errorHandler) throws IOException {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // had to explicitly wire in the error handler for the error handling to work
        factory.setErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public SeekToCurrentErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {

        SeekToCurrentErrorHandler handler = new SeekToCurrentErrorHandler((cr, e) -> {
            log.error(cr.value() + " failed after retries");
            recoverer.accept(cr, e);
        }, new FixedBackOff(0L, 2L));

        return handler;
    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaOperations<String, byte[]> bytesTemplate) {
        return new DeadLetterPublishingRecoverer(bytesTemplate, (r,e) -> {
            return new TopicPartition(r.topic() + ".DLT", r.partition());
        });
    }
}
