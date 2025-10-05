package cl.bancoxyz.analytics.service;

import cl.bancoxyz.analytics.client.LegacyApiClient;
import cl.bancoxyz.analytics.dto.AccountSummaryResultDto;
import cl.bancoxyz.analytics.dto.AnnualAccountDto;
import cl.bancoxyz.analytics.dto.TransactionDto;
import cl.bancoxyz.analytics.event.AccountSummaryRequestEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryProcessor {
  private final LegacyApiClient legacyApiClient;
  private final SummaryStorage storage;

  @Value("${kafka.topics.summary-request:account.summary.request}")
  private String topic;

  @KafkaListener(topics = "${kafka.topics.summary-request:account.summary.request}")
  public void onSummaryRequest(@Payload AccountSummaryRequestEvent event) {
    log.info("Procesando evento async {} para cuenta {} (topic {})", event.getRequestId(),
        event.getAccountId(), topic);
    List<String> warnings = new ArrayList<>();
    try {
      List<TransactionDto> txs = legacyApiClient.transactions(event.getAccountId(), event.getFrom(), event.getTo());
      double totalAmount = txs.stream().mapToDouble(TransactionDto::getAmount).sum();
      int count = txs.size();
      double balance = resolveLatestBalance(event.getAccountId(), warnings);

      AccountSummaryResultDto dto = AccountSummaryResultDto.builder()
          .requestId(event.getRequestId())
          .accountId(event.getAccountId())
          .processedAt(Instant.now())
          .totalAmount(Math.round(totalAmount * 100.0) / 100.0)
          .transactionCount(count)
          .latestBalance(Math.round(balance * 100.0) / 100.0)
          .warnings(warnings)
          .build();
      storage.save(dto);
      log.info("Evento async {} procesado ({} txs)", event.getRequestId(), count);
    } catch (Exception ex) {
      warnings.add("Error procesando evento: " + ex.getMessage());
      AccountSummaryResultDto dto = AccountSummaryResultDto.builder()
          .requestId(event.getRequestId())
          .accountId(event.getAccountId())
          .processedAt(Instant.now())
          .totalAmount(0)
          .transactionCount(0)
          .latestBalance(0)
          .warnings(warnings)
          .build();
      storage.save(dto);
      log.error("Fallo procesando evento {}", event.getRequestId(), ex);
    }
  }

  private double resolveLatestBalance(String accountId, List<String> warnings) {
    try {
      List<AnnualAccountDto> annuals = legacyApiClient.annual(accountId, null);
      Optional<AnnualAccountDto> latest = annuals.stream()
          .max(Comparator.comparingInt(dto -> parseYear(dto.getYear())));
      return latest.map(AnnualAccountDto::getClosingBalance).orElse(0.0);
    } catch (Exception ex) {
      warnings.add("No se pudo obtener balance actual: " + ex.getMessage());
      log.warn("Fallo obteniendo balance para {}", accountId, ex);
      return 0.0;
    }
  }

  private int parseYear(String rawYear) {
    try {
      return Integer.parseInt(rawYear);
    } catch (NumberFormatException ex) {
      return Integer.MIN_VALUE;
    }
  }
}
