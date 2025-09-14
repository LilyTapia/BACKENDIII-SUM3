package cl.bancoxyz.bff.bffmobile.api;

import cl.bancoxyz.bff.bffmobile.dto.*;
import cl.bancoxyz.bff.bffmobile.service.DataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
  public ResponseEntity<?> transactions(@PathVariable String accountId, @RequestParam(required = false) String from,
      @RequestParam(required = false) String to, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var all = service.transactions(accountId, from, to);
    int fromIdx = Math.min(page * size, all.size());
    int toIdx = Math.min(fromIdx + size, all.size());
    List<?> items;
    if ("mobile".equals("mobile")) {
      items = all.subList(fromIdx, toIdx).stream().map(tx -> Map.of("date", tx.getDate(), "type", tx.getType(),
          "amount", Math.round(tx.getAmount() * 100.0) / 100.0)).collect(Collectors.toList());
    } else if ("atm".equals("mobile")) {
      items = all.subList(fromIdx, toIdx).stream()
          .map(tx -> Map.of("date", tx.getDate(), "type", tx.getType(), "amount", tx.getAmount()))
          .collect(Collectors.toList());
    } else {
      items = new ArrayList<>(all.subList(fromIdx, toIdx));
    }
    Map<String, Object> body = new HashMap<>();
    body.put("items", items);
    body.put("total", all.size());
    body.put("page", page);
    body.put("size", size);
    return ResponseEntity.ok(body);
  }

  @GetMapping("/accounts/{accountId}/interests")
  public List<InterestDTO> interests(@PathVariable String accountId, @RequestParam(required = false) Integer month) {
    return service.interests(accountId, month);
  }

  @GetMapping("/accounts/{accountId}/annual")
  public List<AnnualAccountDTO> annual(@PathVariable String accountId, @RequestParam(required = false) String year) {
    return service.annual(accountId, year);
  }
}