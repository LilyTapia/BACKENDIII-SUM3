package cl.bancoxyz.bff.bffweb.api;

import cl.bancoxyz.bff.bffweb.dto.*;
import cl.bancoxyz.bff.bffweb.service.DataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/web")
public class ApiController {
  private final DataService service;

  public ApiController(DataService s) {
    this.service = s;
  }

  @GetMapping("/accounts/{accountId}/transactions")
  @PreAuthorize("hasRole('USER')")
  public PageDto<TransactionDTO> transactions(@PathVariable String accountId,
                                              @RequestParam(required = false) String from,
                                              @RequestParam(required = false) String to,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
    var all = service.transactions(accountId, from, to);
    int fromIdx = Math.min(page * size, all.size());
    int toIdx = Math.min(fromIdx + size, all.size());
    var items = new ArrayList<>(all.subList(fromIdx, toIdx));
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
  public AccountWebDto summary(@PathVariable String accountId,
                               @RequestParam(required = false) String from,
                               @RequestParam(required = false) String to,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size) {
    var txs = service.transactions(accountId, from, to);
    double balance = service.balance(accountId);
    int fromIdx = Math.min(page * size, txs.size());
    int toIdx = Math.min(fromIdx + size, txs.size());
    var items = new ArrayList<>(txs.subList(fromIdx, toIdx));
    return new AccountWebDto(accountId, Math.round(balance * 100.0) / 100.0, items);
  }

  @GetMapping("/admin/metrics")
  @PreAuthorize("hasRole('ADMIN')")
  public String metrics() { return "solo admin"; }
}
