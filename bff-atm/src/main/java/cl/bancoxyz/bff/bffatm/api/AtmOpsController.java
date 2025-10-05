package cl.bancoxyz.bff.bffatm.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Map;
import cl.bancoxyz.bff.bffatm.service.DataService;
import cl.bancoxyz.bff.bffatm.service.TokenService;

@RestController
@RequestMapping("/api/atm")
public class AtmOpsController {
  private final TokenService tokens;
  private final DataService data;

  public AtmOpsController(TokenService tokens, DataService data) {
    this.tokens = tokens;
    this.data = data;
  }

  @PostMapping("/login")
  @PreAuthorize("hasAuthority('SCOPE_bff.atm.write')")
  public Map<String, String> login(@RequestHeader("X-CARD") String card, @RequestHeader("X-PIN") String pin) {
    // In real life validate card+pin; here always issue a token
    String token = tokens.issueToken(card);
    return Map.of("status", "OK", "token", token);
  }

  @PostMapping("/withdraw")
  @PreAuthorize("hasAuthority('SCOPE_bff.atm.write')")
  public ResponseEntity<?> withdraw(@RequestHeader("X-ATM-TOKEN") String token,
                                    @RequestParam String accountId,
                                    @RequestParam double amount,
                                    @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
    if (!tokens.isValid(token)) {
      return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
    }
    // Mock withdrawal result; in real flow, call core
    double current = data.balance(accountId);
    double newBalance = Math.round((current - amount) * 100.0) / 100.0;
    return ResponseEntity.ok(Map.of("accountId", accountId, "withdrawn", amount, "newBalance", newBalance));
  }

  @GetMapping("/balance")
  @PreAuthorize("hasAuthority('SCOPE_bff.atm.read')")
  public ResponseEntity<?> balance(@RequestHeader("X-ATM-TOKEN") String token, @RequestParam String accountId) {
    if (!tokens.isValid(token)) {
      return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
    }
    double balance = Math.round(data.balance(accountId) * 100.0) / 100.0;
    return ResponseEntity.ok(new cl.bancoxyz.bff.bffatm.dto.AccountAtmDto(accountId, balance));
  }
}
