package com.travelbook.servicenotification.config;

import com.travelbook.servicenotification.messaging.NotificationEmailRequestedEvent;
import com.travelbook.servicenotification.messaging.NotificationKafkaListener;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {

    @Bean
    public ConsumerFactory<String, NotificationEmailRequestedEvent> consumerFactory(
            Environment environment) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getProperty("spring.kafka.bootstrap-servers",
                        "localhost:9092"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                environment.getProperty("spring.kafka.consumer.group-id",
                        NotificationKafkaListener.GROUP_ID));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                environment.getProperty("spring.kafka.consumer.auto-offset-reset",
                        "earliest"));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                Boolean.parseBoolean(environment.getProperty(
                        "spring.kafka.consumer.enable-auto-commit", "false")));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.travelbook.*");

        JacksonJsonDeserializer<NotificationEmailRequestedEvent> jsonDeserializer =
                new JacksonJsonDeserializer<>(NotificationEmailRequestedEvent.class);
        ErrorHandlingDeserializer<NotificationEmailRequestedEvent> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequestedEvent>
            kafkaListenerContainerFactory(
                    ConsumerFactory<String, NotificationEmailRequestedEvent> consumerFactory,
                    KafkaTemplate<?, ?> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequestedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(2000L, 2L)));
        return factory;
    }
}
