package cl.bancoxyz.bff.bffweb.service;

import cl.bancoxyz.bff.bffweb.client.LegacyApiClient;
import cl.bancoxyz.bff.bffweb.dto.AnnualAccountDTO;
import cl.bancoxyz.bff.bffweb.dto.InterestDTO;
import cl.bancoxyz.bff.bffweb.dto.TransactionDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DataService {
  private final LegacyApiClient legacyApiClient;
  private final String fallbackMessage;

  public DataService(LegacyApiClient legacyApiClient,
                     @Value("${bff.web.fallback-message:Servicio temporalmente no disponible.}") String fallbackMessage) {
    this.legacyApiClient = legacyApiClient;
    this.fallbackMessage = fallbackMessage;
  }

  @CircuitBreaker(name = "legacyApi", fallbackMethod = "transactionsFallback")
  @Retry(name = "legacyApi")
  public List<TransactionDTO> transactions(String accountId, String from, String to) {
    return legacyApiClient.transactions(accountId, from, to);
  }

  @CircuitBreaker(name = "legacyApi", fallbackMethod = "interestsFallback")
  @Retry(name = "legacyApi")
  public List<InterestDTO> interests(String accountId, Integer month) {
    return legacyApiClient.interests(accountId, month);
  }

  @CircuitBreaker(name = "legacyApi", fallbackMethod = "annualFallback")
  @Retry(name = "legacyApi")
  public List<AnnualAccountDTO> annual(String accountId, String year) {
    return legacyApiClient.annual(accountId, year);
  }

  @CircuitBreaker(name = "legacyApi", fallbackMethod = "balanceFallback")
  @Retry(name = "legacyApi")
  public double balance(String accountId) {
    List<AnnualAccountDTO> annuals = legacyApiClient.annual(accountId, null);
    Optional<AnnualAccountDTO> latest = annuals.stream()
        .max(Comparator.comparingInt(dto -> parseYear(dto.getYear())));
    if (latest.isPresent()) {
      return latest.get().getClosingBalance();
    }
    return legacyApiClient.transactions(accountId, null, null).stream()
        .mapToDouble(TransactionDTO::getAmount)
        .sum();
  }

  private int parseYear(String rawYear) {
    try {
      return Integer.parseInt(rawYear);
    } catch (NumberFormatException ex) {
      return Integer.MIN_VALUE;
    }
  }

  private List<TransactionDTO> transactionsFallback(String accountId, String from, String to, Throwable throwable) {
    log.warn("Fallback de transacciones para {}: {}", accountId, throwable.getMessage());
    return List.of();
  }

  private List<InterestDTO> interestsFallback(String accountId, Integer month, Throwable throwable) {
    log.warn("Fallback de intereses para {}: {}", accountId, throwable.getMessage());
    return List.of();
  }

  private List<AnnualAccountDTO> annualFallback(String accountId, String year, Throwable throwable) {
    log.warn("Fallback anual para {}: {}", accountId, throwable.getMessage());
    return List.of(AnnualAccountDTO.builder()
        .accountId(accountId)
        .year(year != null ? year : "N/A")
        .openingBalance(0)
        .closingBalance(0)
        .build());
  }

  private double balanceFallback(String accountId, Throwable throwable) {
    log.warn("Fallback de balance para {}: {} - {}", accountId, fallbackMessage, throwable.getMessage());
    return 0;
  }
}
