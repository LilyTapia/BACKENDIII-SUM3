package cl.bancoxyz.bff.bffatm.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/atm")
public class AtmOpsController {
  @PostMapping("/login")
  public Map<String, String> login(@RequestHeader("X-CARD") String card, @RequestHeader("X-PIN") String pin) {
    return Map.of("status", "OK", "token", "atm-demo-token");
  }

  @PostMapping("/withdraw")
  public ResponseEntity<?> withdraw(@RequestHeader("X-ATM-TOKEN") String token, @RequestParam String accountId,
      @RequestParam double amount, @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
    return ResponseEntity.ok(Map.of("accountId", accountId, "withdrawn", amount, "newBalance", 1234.56));
  }

  @GetMapping("/balance")
  public Map<String, Object> balance(@RequestHeader("X-ATM-TOKEN") String token, @RequestParam String accountId) {
    return Map.of("accountId", accountId, "balance", 1234.56);
  }
}