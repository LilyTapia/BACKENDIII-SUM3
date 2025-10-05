package cl.bancoxyz.bff.bffatm.api;

import cl.bancoxyz.bff.bffatm.dto.*;
import cl.bancoxyz.bff.bffatm.service.DataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/atm")
public class ApiController {
  private final DataService service;

  public ApiController(DataService s) {
    this.service = s;
  }

  @GetMapping("/accounts/{accountId}/transactions")
  @PreAuthorize("hasAuthority('SCOPE_bff.atm.read')")
  public PageDto<TransactionSlimDto> transactions(@PathVariable("accountId") String accountId,
                                                  @RequestParam(name = "from", required = false) String from,
                                                  @RequestParam(name = "to", required = false) String to,
                                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                                  @RequestParam(name = "size", defaultValue = "20") int size) {
    var all = service.transactions(accountId, from, to);
    int fromIdx = Math.min(page * size, all.size());
    int toIdx = Math.min(fromIdx + size, all.size());
    var items = all.subList(fromIdx, toIdx).stream()
        .map(tx -> new TransactionSlimDto(tx.getDate(), tx.getType(), tx.getAmount()))
        .collect(Collectors.toList());
    return new PageDto<>(items, page, size, all.size());
  }

  @GetMapping("/accounts/{accountId}/interests")
  @PreAuthorize("hasAuthority('SCOPE_bff.atm.read')")
  public List<InterestDTO> interests(@PathVariable("accountId") String accountId,
                                     @RequestParam(name = "month", required = false) Integer month) {
    return service.interests(accountId, month);
  }

  @GetMapping("/accounts/{accountId}/annual")
  @PreAuthorize("hasAuthority('SCOPE_bff.atm.read')")
  public List<AnnualAccountDTO> annual(@PathVariable("accountId") String accountId,
                                       @RequestParam(name = "year", required = false) String year) {
    return service.annual(accountId, year);
  }

  @GetMapping("/admin/metrics")
  @PreAuthorize("hasAuthority('SCOPE_bff.atm.admin')")
  public String metrics() { return "solo admin"; }
}
