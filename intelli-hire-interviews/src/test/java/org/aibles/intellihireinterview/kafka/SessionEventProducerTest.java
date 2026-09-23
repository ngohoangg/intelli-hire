package org.aibles.intellihireinterview.kafka;

import org.aibles.intellihireinterview.dto.event.SessionCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SessionEventProducerTest {

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    SessionEventProducer producer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(producer, "sessionCompletedTopic", "session_completed");
    }

    @Test
    void publishSessionCompleted_sendsToCorrectTopic() {
        var event = SessionCompletedEvent.builder()
            .sessionId(1L).userId("42").finalStatus("COMPLETED").build();

        when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        producer.publishSessionCompleted(event);

        verify(kafkaTemplate).send(eq("session_completed"), eq("1"), eq(event));
    }
}
