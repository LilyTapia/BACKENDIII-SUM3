package cl.bancoxyz.bff.bffweb.service;

import cl.bancoxyz.bff.bffweb.event.AccountSummaryRequestEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryEventPublisher {
  private final KafkaTemplate<String, AccountSummaryRequestEvent> kafkaTemplate;

  @Value("${kafka.topics.summary-request:account.summary.request}")
  private String topic;

  public String publish(String accountId, String from, String to, String channel) {
    String requestId = UUID.randomUUID().toString();
    AccountSummaryRequestEvent event = AccountSummaryRequestEvent.builder()
        .requestId(requestId)
        .accountId(accountId)
        .from(from)
        .to(to)
        .requestedAt(Instant.now())
        .channel(channel)
        .build();
    kafkaTemplate.send(topic, accountId, event).whenComplete((result, ex) -> {
      if (ex != null) {
        // Si algo falla queremos dejar un rastro claro con el identificador del request
        log.error("Error publicando evento async {}", requestId, ex);
      } else if (result != null) {
        // Confirmamos la publicación con la partición asignada, útil para auditoría
        log.info("Evento async {} enviado a {}@partition {}", requestId, topic,
            result.getRecordMetadata().partition());
      }
    });
    return requestId;
  }
}
