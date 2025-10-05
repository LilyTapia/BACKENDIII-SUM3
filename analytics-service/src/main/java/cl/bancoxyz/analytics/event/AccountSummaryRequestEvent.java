package cl.bancoxyz.analytics.event;

import java.time.Instant;
import lombok.Data;

@Data
public class AccountSummaryRequestEvent {
  private String requestId;
  private String accountId;
  private String from;
  private String to;
  private Instant requestedAt;
  private String channel;
}
