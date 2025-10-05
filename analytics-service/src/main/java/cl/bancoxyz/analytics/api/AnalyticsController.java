package cl.bancoxyz.analytics.api;

import cl.bancoxyz.analytics.dto.AccountSummaryResultDto;
import cl.bancoxyz.analytics.service.SummaryStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

  private final SummaryStorage storage;

  @GetMapping("/summary/{requestId}")
  public ResponseEntity<AccountSummaryResultDto> byRequestId(@PathVariable String requestId) {
    return storage.findByRequestId(requestId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/summary/account/{accountId}/latest")
  public ResponseEntity<AccountSummaryResultDto> latestForAccount(@PathVariable String accountId) {
    return storage.latestForAccount(accountId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
