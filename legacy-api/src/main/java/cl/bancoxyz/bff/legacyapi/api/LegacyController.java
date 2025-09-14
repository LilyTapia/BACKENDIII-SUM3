package cl.bancoxyz.bff.legacyapi.api;

import cl.bancoxyz.bff.legacyapi.dto.*;
import cl.bancoxyz.bff.legacyapi.service.LegacyService;
import org.springframework.web.bind.annotation.*;

import com.opencsv.exceptions.CsvValidationException;

import java.util.*;

@RestController
@RequestMapping("/legacy")
public class LegacyController {
  private final LegacyService service;

  public LegacyController(LegacyService s) {
    this.service = s;
  }

  @GetMapping("/transactions")
  public List<TransactionDTO> transactions(@RequestParam(name = "accountId", required = false) String accountId,
      @RequestParam(name = "from", required = false) String from,
      @RequestParam(name = "to", required = false) String to) {
    return service.transactions(accountId, from, to);
  }

  @GetMapping("/interests")
  public List<InterestDTO> interests(@RequestParam(name = "accountId", required = false) String accountId,
      @RequestParam(name = "month", required = false) Integer month) {
    return service.interests(accountId, month);
  }

  @GetMapping("/annual")
  public List<AnnualAccountDTO> annual(@RequestParam(name = "accountId", required = false) String accountId,
      @RequestParam(name = "year", required = false) String year) {
    return service.annual(accountId, year);
  }
}
