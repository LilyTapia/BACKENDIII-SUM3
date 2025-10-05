package cl.bancoxyz.bff.bffweb.event;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountSummaryRequestEvent {
  String requestId;
  String accountId;
  String from;
  String to;
  Instant requestedAt;
  String channel;
}
