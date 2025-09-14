package cl.bancoxyz.bff.bffmobile.service;

import cl.bancoxyz.bff.bffmobile.dto.*;
import cl.bancoxyz.bff.bffmobile.client.LegacyApiClient;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DataService {
  private final LegacyApiClient legacy;

  public DataService(LegacyApiClient l) {
    this.legacy = l;
  }

  public List<TransactionDTO> transactions(String accountId, String from, String to) {
    return legacy.transactions(accountId, from, to);
  }

  public List<InterestDTO> interests(String accountId, Integer month) {
    return legacy.interests(accountId, month);
  }

  public List<AnnualAccountDTO> annual(String accountId, String year) {
    return legacy.annual(accountId, year);
  }

  public double balance(String accountId) {
    // Prefer latest annual closing balance if available
    var annuals = legacy.annual(accountId, null);
    Optional<AnnualAccountDTO> latest = annuals.stream()
        .max(Comparator.comparingInt(a -> {
          try { return Integer.parseInt(a.getYear()); } catch (Exception e) { return Integer.MIN_VALUE; }
        }));
    if (latest.isPresent()) {
      return latest.get().getClosingBalance();
    }
    // Fallback: sum all transactions
    return legacy.transactions(accountId, null, null).stream()
        .mapToDouble(TransactionDTO::getAmount)
        .sum();
  }
}
