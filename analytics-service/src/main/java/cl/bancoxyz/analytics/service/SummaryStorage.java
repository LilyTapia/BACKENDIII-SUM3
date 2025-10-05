package cl.bancoxyz.analytics.service;

import cl.bancoxyz.analytics.dto.AccountSummaryResultDto;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SummaryStorage {
  private final Map<String, AccountSummaryResultDto> byRequestId = new ConcurrentHashMap<>();
  private final Map<String, String> latestByAccount = new ConcurrentHashMap<>();

  public void save(AccountSummaryResultDto dto) {
    byRequestId.put(dto.getRequestId(), dto);
    latestByAccount.put(dto.getAccountId(), dto.getRequestId());
  }

  public Optional<AccountSummaryResultDto> findByRequestId(String requestId) {
    return Optional.ofNullable(byRequestId.get(requestId));
  }

  public Optional<AccountSummaryResultDto> latestForAccount(String accountId) {
    String requestId = latestByAccount.get(accountId);
    if (requestId == null) {
      return Optional.empty();
    }
    return findByRequestId(requestId);
  }
}
