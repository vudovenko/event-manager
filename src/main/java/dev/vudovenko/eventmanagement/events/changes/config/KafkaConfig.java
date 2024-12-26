package dev.vudovenko.eventmanagement.events.changes.config;

import dev.vudovenko.eventmanagement.events.changes.dto.EventChangeDto;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaConfig {

    /**
     * Создается KafkaTemplate для отправки сообщений в Kafka
     *
     * @param kafkaProperties - настройки Kafka из application.properties
     * @return KafkaTemplate для отправки сообщений
     */
    @Bean
    public KafkaTemplate<Long, EventChangeDto> kafkaTemplate(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties(
                new DefaultSslBundleRegistry()
        );

        ProducerFactory<Long, EventChangeDto> producerFactory =
                new DefaultKafkaProducerFactory<>(producerProperties);

        return new KafkaTemplate<>(producerFactory);
    }
}
