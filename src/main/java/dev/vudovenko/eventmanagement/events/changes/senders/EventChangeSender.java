package dev.vudovenko.eventmanagement.events.changes.senders;

import dev.vudovenko.eventmanagement.events.changes.dto.EventChangeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
@RequiredArgsConstructor
public class EventChangeSender {

    private final KafkaTemplate<Long, EventChangeDto> kafkaTemplate;

    public void sendEvent(EventChangeDto eventChangeDTO) {
        log.info("Sending event: event = {}", eventChangeDTO);
        CompletableFuture<SendResult<Long, EventChangeDto>> result = kafkaTemplate.send(
                "events-changes-topic",
                eventChangeDTO.eventId(),
                eventChangeDTO
        );

        result.thenAccept(sendResult -> log.info("Send successful"));
    }
}
