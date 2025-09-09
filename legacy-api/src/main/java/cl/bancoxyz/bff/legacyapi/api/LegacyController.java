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
  public List<TransactionDTO> transactions(@RequestParam(required = false) String accountId,
      @RequestParam(required = false) String from, @RequestParam(required = false) String to) throws CsvValidationException {
    return service.transactions(accountId, from, to);
  }

  @GetMapping("/interests")
  public List<InterestDTO> interests(@RequestParam(required = false) String accountId,
      @RequestParam(required = false) Integer month) throws CsvValidationException {
    return service.interests(accountId, month);
  }

  @GetMapping("/annual")
  public List<AnnualAccountDTO> annual(@RequestParam(required = false) String accountId,
      @RequestParam(required = false) String year) throws CsvValidationException {
    return service.annual(accountId, year);
  }
}