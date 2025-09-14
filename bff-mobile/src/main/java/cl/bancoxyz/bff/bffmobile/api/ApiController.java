package cl.bancoxyz.bff.bffmobile.api;

import cl.bancoxyz.bff.bffmobile.dto.*;
import cl.bancoxyz.bff.bffmobile.service.DataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mobile")
public class ApiController {
  private final DataService service;

  public ApiController(DataService s) {
    this.service = s;
  }

  @GetMapping("/accounts/{accountId}/transactions")
  @PreAuthorize("hasRole('USER')")
  public PageDto<TransactionSlimDto> transactions(@PathVariable String accountId,
                                                  @RequestParam(required = false) String from,
                                                  @RequestParam(required = false) String to,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
    var all = service.transactions(accountId, from, to);
    int fromIdx = Math.min(page * size, all.size());
    int toIdx = Math.min(fromIdx + size, all.size());
    var items = all.subList(fromIdx, toIdx).stream()
        .map(tx -> new TransactionSlimDto(tx.getDate(), tx.getType(), Math.round(tx.getAmount() * 100.0) / 100.0))
        .collect(Collectors.toList());
    return new PageDto<>(items, page, size, all.size());
  }

  @GetMapping("/accounts/{accountId}/interests")
  @PreAuthorize("hasRole('USER')")
  public List<InterestDTO> interests(@PathVariable String accountId, @RequestParam(required = false) Integer month) {
    return service.interests(accountId, month);
  }

  @GetMapping("/accounts/{accountId}/annual")
  @PreAuthorize("hasRole('USER')")
  public List<AnnualAccountDTO> annual(@PathVariable String accountId, @RequestParam(required = false) String year) {
    return service.annual(accountId, year);
  }

  @GetMapping("/accounts/{accountId}/summary")
  @PreAuthorize("hasRole('USER')")
  public AccountMobileDto summary(@PathVariable String accountId) {
    double balance = service.balance(accountId);
    return new AccountMobileDto(accountId, Math.round(balance * 100.0) / 100.0);
  }

  @GetMapping("/admin/metrics")
  @PreAuthorize("hasRole('ADMIN')")
  public String metrics() { return "solo admin"; }
}
