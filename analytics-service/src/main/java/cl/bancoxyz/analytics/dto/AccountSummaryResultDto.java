package cl.bancoxyz.analytics.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountSummaryResultDto {
  String requestId;
  String accountId;
  Instant processedAt;
  double totalAmount;
  int transactionCount;
  double latestBalance;
  List<String> warnings;
}
